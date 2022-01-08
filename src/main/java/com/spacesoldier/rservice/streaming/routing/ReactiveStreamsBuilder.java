package com.spacesoldier.rservice.streaming.routing;

import com.spacesoldier.rservice.streaming.manage.FluxWiresManager;
import com.spacesoldier.rservice.streaming.routing.entities.stream.StreamNode;
import com.spacesoldier.rservice.streaming.transformers.flux.OneToManyValueTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.Function;

// This class implements the logic of the reactive data processing chains construction
// The reason for building this mechanics is the need to concentrate its logic at one point
// instead of using the autowiring too often (almost everywhere) and writing too many same looking
// configurations and beans on one side and manually definition of the reactive logic chains on the other
public class ReactiveStreamsBuilder {

    public static final String unitName = "stream builder";
    private static final Logger logger = LoggerFactory.getLogger("rx builder");

    private FluxWiresManager fluxManager;

    public ReactiveStreamsBuilder(FluxWiresManager manager){
        fluxManager = manager;
    }

    // here we store the building blocks of inner structure
    // until they used for building the reactive streams definitions
    List<StreamNode> allStreamNodes = new LinkedList<>();

    // let's collect the stream names here
    Set<String> streamNames = new HashSet<>();

    // here we store the lists of nodes corresponding to the names
    // of logic chains we build
    Map<String, List<StreamNode>> streams = new HashMap<>();

    // this is the main routing table
    // which allow to match the object class to the stream name
    // where a subscriber consumes the objects of this type
    Map<Class, List<String>> routingMap = new HashMap<>();

    // register a transformation node provided by user
    public StreamNode register(StreamNode newNode){
        allStreamNodes.add(newNode);
        return newNode;
    }

    // register a transformation of the stream defined by its name
    public StreamNode register(Class inputType, Class outputType, Function transformation, String streamName, String nodeName){
        StreamNode newNode = StreamNode.builder()
                                            .streamName(streamName)
                                            .nodeName(nodeName)
                                            .transformationInputType(inputType)
                                            .transformationOutputType(outputType)
                                            .transformation(transformation)
                                        .build();

        allStreamNodes.add( newNode );

        return newNode;
    };

    public StreamNode register(Class inputType, Function transformation, String streamName, String nodeName){
        return register(
                inputType,
                null,
                transformation,
                streamName,
                nodeName
        );
    }

    public StreamNode register(Class inputType, Function transformation, String streamName){
        String nodeName = inputType.getName();
        return register(
                inputType,
                transformation,
                streamName,
                nodeName
        );
    }


    private String streamNameTemplate = "%sStream";

    public StreamNode register(Class inputType, Function transformation){
        String nodeName = inputType.getName();
        return register(
                inputType,
                transformation,
                String.format(streamNameTemplate,nodeName),
                nodeName
        );
    }

    public int buildStream(String streamName){
        int result = 0;
        List<StreamNode> nodesOfStream = new LinkedList<>();

        // let's extract the stream nodes from their initial place
        // to the structure where the stream definition appears to be constructed
        Iterator<StreamNode> nodesIterator = allStreamNodes.iterator();
        while (nodesIterator.hasNext()){
            StreamNode node = nodesIterator.next();
            if (node.getStreamName().equals(streamName)){
                if (!nodesOfStream.contains(node)){
                    nodesOfStream.add(node);
                }

                nodesIterator.remove();
            }
        }

        // firstly we build a directed graph of nodes
        // using the information about their input and output types
        // streamNode represents the edge of a graph
        // and the classes (input/output types) represent the graph nodes

        // firstly let's build a sort of adjacency matrix
        List<List<Class>> adjacencyMatrix = new ArrayList<>();

        // then take a list of stream nodes and build the matrix
        for (int i=0; i<nodesOfStream.size(); i++){
            adjacencyMatrix.add(i,new ArrayList<>());
            List matrixRow = adjacencyMatrix.get(i);
            for (int j=0; j< nodesOfStream.size(); j++){
                matrixRow.add(null);
            }
        }

        // next let's build a sort of index for the classes (input/output types)
        // which are defined for transformers we plan to connect into a chain
        // we store our index into a list
        List<Class> nodeIndex = buildIndex(nodesOfStream);

        Set<Class> nodeInputs = new HashSet<>();
        Set<Class> nodeOutputs = new HashSet<>();

        // then we fill the adjacency matrix using our index
        for (StreamNode edge: nodesOfStream){
            Class edgeDest = edge.getTransformationOutputType();
            if (edgeDest != null){
                int inputIndex = nodeIndex.indexOf(
                                                    edge.getTransformationInputType()
                                            );
                int outputIndex = nodeIndex.indexOf(
                                                        edge.getTransformationOutputType()
                                                    );
                adjacencyMatrix.get(inputIndex).set(
                                                        outputIndex,
                                                        edge.getTransformationOutputType()
                                                    );
            }
        }

        StringBuilder adjMatrixView = prepareAdjMatrixView(adjacencyMatrix, nodeIndex);

        logger.info(String.format(
                "[%s]: adjacency matrix for stream %s\n%s",
                unitName.toUpperCase(),
                streamName,
                adjMatrixView
        ));

        // also let's write down the input and output types of transformations
        // to use it for root detection
        for (StreamNode node: nodesOfStream){
            if (node.getTransformationInputType() != null){
                nodeInputs.add(node.getTransformationInputType());
            }
            if (node.getTransformationOutputType() != null){
                nodeOutputs.add(node.getTransformationOutputType());
            }
        }

        // then let's make a couple indexes

        Map<Class, StreamNode> indexNodesByInputType = new HashMap<>();
        Map<Class, StreamNode> indexNodesByOutputType = new HashMap<>();

        // fill our indexes with stream nodes
        // as a result we are able to retrieve the node by its input or output type
        for (StreamNode node: nodesOfStream){

            if (node.getTransformationInputType() != null){
                indexNodesByInputType.put(node.getTransformationInputType(),node);
            }

            if (node.getTransformationOutputType() != null){
                indexNodesByOutputType.put(node.getTransformationOutputType(), node);
            }
        }

        // next step is detecting a root node
        // assume the root node connects as the first element in logic chain,
        // so it's the only node which input type does not match any output type
        // of provided transformers
        // otherwise we have the cycled reactive chain which is an equivalent of the infinite loop
        // which we treat as a structural error and as a result such a chain should not be built

        // to find out which node does not match anyone's output
        // and to do this we intersect the set of input types with the set of output types
        Set<Class> intersection = new HashSet<>(nodeInputs);
        intersection.removeAll(nodeOutputs);

        // let's see if we found any candidates to be the root node of our graph
        if (intersection.size() > 0){
            logger.info(
                    String.format(
                            logMsgTemplate,
                            unitName.toUpperCase(),
                            String.format(
                                    "found the root node candidates of %s stream:\n %s",
                                    streamName,
                                    intersection

                            )
                    )
            );
            logger.info("building the logic chain");

            // then we perform a breadth-first traverse of the stream nodes graph
            List<Class> rootCandidates = new ArrayList(intersection);
            List<StreamNode> logicChainDef = buildLogicChain(
                                                                adjacencyMatrix,
                                                                nodeIndex,
                                                                indexNodesByInputType,
                                                                rootCandidates
                                                            );

            assembleReactiveChain(streamName, logicChainDef);

            result = 0;

        } else {
            // all input types are found among output types, so we have a cycled chain
            // which will send the message to itself infinitely
            // we should not build this chain

            result = -1;
        }

        // we need to apply topological sorting to our stream nodes
        // to make sure they follow each other in order of dependence
        // according to their input data types

        return result;
    }

    private void assembleReactiveChain(String streamName, List<StreamNode> logicChainDef) {
        Flux currentStream = fluxManager.getStream(streamName);

        for (StreamNode mapping: logicChainDef){
            // build a one-to-many transformer and apply it to the current flux

            OneToManyValueTransformer newMapper = new OneToManyValueTransformer(
                    mapping.getNodeName(),
                    new HashMap<>(){
                        {
                            put(mapping.getTransformationInputType(),mapping.getTransformation());
                        }
                    }
            );

            currentStream = currentStream.flatMap(newMapper);
        }

        currentStream.subscribe();
    }

    private List<StreamNode> buildLogicChain(List<List<Class>> adjacencyMatrix, List<Class> nodeIndex, Map<Class, StreamNode> indexNodesByInputType, List<Class> rootCandidates) {
        // here we put the stream nodes in the proper order
        List<StreamNode> logicChainDef = new ArrayList<>();

        List<Class> visitedVertices = new ArrayList<>();
        LinkedList<Class> tmpQueue = new LinkedList<>();

        visitedVertices.add(rootCandidates.get(0));
        tmpQueue.add(rootCandidates.get(0));

        while (tmpQueue.size() != 0){
            Class currentNode = tmpQueue.poll();
            int matrixRowNumber = nodeIndex.indexOf(currentNode);
            ListIterator<Class> iterator = adjacencyMatrix.get(matrixRowNumber).listIterator();
            while (iterator.hasNext()){
                Class vertex = iterator.next();
                if (vertex != null){
                    if (!visitedVertices.contains(vertex)){
                        visitedVertices.add(vertex);
                        tmpQueue.add(vertex);
                    }
                }
            }
        }

        StringBuilder verticesList = new StringBuilder();
        for (Class vertice: visitedVertices){
            verticesList.append(String.format("%s\n",vertice.getSimpleName()));
        }
        logger.info(
                String.format(
                        "[%s]: inputs in order\n%s",
                        unitName.toUpperCase(),
                        verticesList
                )
        );

        // fill logic chain definition with stream nodes
        // using visited vertices list as a source of order
        // and the inputs index as a way to retrieve proper stream node
        for (Class vertice: visitedVertices){
            StreamNode properNode = indexNodesByInputType.get(vertice);
            logicChainDef.add(properNode);
        }
        return logicChainDef;
    }

    private StringBuilder prepareAdjMatrixView(List<List<Class>> adjacencyMatrix, List<Class> nodeIndex) {
        StringBuilder adjMatrixView = new StringBuilder();
        List<Class> inputsColumn = new ArrayList<>(nodeIndex);
        for (int row = 0; row < adjacencyMatrix.size(); row++){
            for (int col = 0; col < adjacencyMatrix.size(); col++){
                if (col == 0){
                    Class inputClass = inputsColumn.get(row);
                    adjMatrixView.append(
                            String.format("%s\t", inputClass.getSimpleName())
                    );
                }
                Class cellValue = adjacencyMatrix.get(row).get(col);
                String cellValueStr = cellValue == null ?
                                        "---------->"               :
                                        cellValue.getSimpleName()   ;
                String cellView = String.format("%s\t", cellValueStr);
                adjMatrixView.append(cellView);
                if (col == adjacencyMatrix.size() - 1){
                    adjMatrixView.append("\n");
                }
            }
        }
        return adjMatrixView;
    }

    private List<Class> buildIndex(List<StreamNode> nodesOfStream) {
        List<Class> nodeIndex = new ArrayList<>();
        for (StreamNode transformer: nodesOfStream){
            // consider stream (transformer) node represents an edge of the graph
            // then we treat its input type as src and its output type as dest
            // or, in other words, let's treat input as "from" and output as "to"
            Class src = transformer.getTransformationInputType();
            Class dest = transformer.getTransformationOutputType();

            // when we did not define the output type,
            // we cannot put anything into the index
            if (dest != null) {
                // then we add the types into our index
                if (!nodeIndex.contains(src)) {
                    nodeIndex.add(src);
                }
                if (!nodeIndex.contains(dest)) {
                    nodeIndex.add(dest);
                }
            }
        }
        return nodeIndex;
    }

    private static String logMsgTemplate = "[%s]: %s";

    public void buildStreams(){


        for(StreamNode node: allStreamNodes){
            String streamName = node.getStreamName();
            Class inputType = node.getTransformationInputType();

            streamNames.add(streamName);
            addNodeToStreamDefinition(node, streamName);
            addRouteToStreamsByClass(streamName, inputType);

        }

        logger.info(
                String.format(logMsgTemplate,
                            unitName.toUpperCase(),
                            String.format(
                                    "built the following routing table: \n %s",
                                    routingMap
                            )
                        )
        );


        for (String streamName: streamNames){
            buildStream(streamName);
        }

    }

    private void addNodeToStreamDefinition(StreamNode node, String streamName) {
        // create new stream when needed
        if (!streams.containsKey(streamName)){
            streams.put(streamName,new ArrayList<>());
        }

        streams.get(streamName).add(node);
    }

    private void addRouteToStreamsByClass(String streamName, Class inputType) {
        if (!routingMap.containsKey(inputType)){
            routingMap.put(inputType,new ArrayList<>());
        }

        // to prevent duplication let's check if we already map the given input type
        // into the stream with given streamName
        if (!routingMap.get(inputType).contains(streamName)){
            routingMap.get(inputType).add(streamName);
        }
    }


}
