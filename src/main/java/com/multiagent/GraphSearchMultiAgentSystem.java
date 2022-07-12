package com.multiagent;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.multiagent.datastructure.Edge;
import com.multiagent.datastructure.Graph;
import com.multiagent.datastructure.Vertex;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface GraphSearchMultiAgentSystem {
    interface GraphSearchProtocol {
    }

    class GraphSearchAgent extends AbstractBehavior<GraphSearchProtocol> {
        public static final class ProcessGraph implements GraphSearchProtocol {
            public Graph graph;
            public Vertex<String> source;

            public ProcessGraph(Graph graph, Vertex<String> source) {
                this.source = source;
                this.graph = graph;
                this.graph.Dijkstra(this.source);
            }
        }

        public static final class ReturnRoute implements GraphSearchProtocol {
            public Graph graph;
            public Vertex<String> destination;

            public ReturnRoute(Graph graph, Vertex<String> destination) {
                this.destination = destination;
                this.graph = graph;
            }
        }

        private Vertex<String> _source;
        private Graph _graph;

        public static Behavior<GraphSearchProtocol> create() {
            return Behaviors.setup(GraphSearchAgent::new);
        }

        private GraphSearchAgent(ActorContext<GraphSearchProtocol> context) {
            super(context);
        }

        public Receive<GraphSearchProtocol> createReceive() {
            return newReceiveBuilder().onMessage(GraphSearchProtocol.class, this::onReceipt).build();
        }

        private void processGraph(ProcessGraph msg) {
            _source = msg.source;
            _graph = msg.graph;
            getContext().getLog().info("Finding all routes from source " + _source + " : " + _graph.getShortestDistance());
        }

        private void returnRoute(ReturnRoute msg) {
            getContext().getLog().info("Returning route to from " + _source + " to " + msg.destination + " : " + _graph.getPath(msg.destination));
        }

        private Behavior<GraphSearchProtocol> onReceipt(GraphSearchProtocol msgCommand) {
            if (msgCommand instanceof ProcessGraph)
                processGraph((ProcessGraph) msgCommand);
            else if (msgCommand instanceof ReturnRoute)
                returnRoute((ReturnRoute) msgCommand);
            return this;
        }
    }

    class GraphSearchOverseer extends AbstractBehavior<GraphSearchProtocol> {
        public static final class LoadGraph implements GraphSearchProtocol {
            public final String fileName;

            public LoadGraph(String strFileName) {
                this.fileName = strFileName;
            }
        }

        public static final class FindRoute implements GraphSearchProtocol {
            public final String source;
            public final String destination;

            public FindRoute(String source, String dest) {
                this.source = source;
                this.destination = dest;
            }
        }

        public static Behavior<GraphSearchProtocol> create() {
            return Behaviors.setup(GraphSearchOverseer::new);
        }

        private final ArrayList<ActorRef<GraphSearchProtocol>> lstSearchers = new ArrayList<>();

        private GraphSearchOverseer(ActorContext<GraphSearchProtocol> context) {
            super(context);
        }

        @Override
        public Receive<GraphSearchProtocol> createReceive() {
            return newReceiveBuilder().onMessage(GraphSearchProtocol.class, this::onReceipt).build();
        }

        public Vertex<String> findVertex(String tVal, List<Vertex<String>> allVertices) {
            Vertex<String> val = null;
            for (Vertex<String> v : allVertices) {
                if (v.getValue().equals(tVal)) {
                    val = v;
                }
            }
            return val;
        }

        private Graph loadGraphFile(String strFilename) {
            Scanner sc = null;
            String vertices = "";
            String edges = "";
            try {
                sc = new Scanner(new File(strFilename));
                String val = "";
                while (sc.hasNextLine()) {
                    val = sc.nextLine();
                    if (val.contains("Vertices:")) {
                        vertices = val.substring(9);
                    } else if (val.contains("Edges:")) {
                        edges = val.substring(6);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            vertices = vertices.replaceAll(" ", "");
            vertices = vertices.replace("[", "");
            vertices = vertices.replace("]", "");

            edges = edges.replaceAll(" ", "");
            edges = edges.replace("[", "");
            edges = edges.replace("]", "");

            List<String> listVertices = Arrays.asList(vertices.split(","));
            List<String> listEdges = Arrays.asList(edges.split(","));

            List<Vertex<String>> allVertices = new ArrayList<>();
            List<Edge<String>> allEdges = new ArrayList<>();

            for (String v : listVertices) {
                allVertices.add(new Vertex<String>(v));
            }

            for (String e : listEdges) {
                int cost = 0;
                String v1, v2 = "";
                v1 = String.valueOf(e.charAt(0));
                v2 = String.valueOf(e.charAt(e.length() - 1));
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(e);
                while (m.find()) {
                    cost = Integer.parseInt(m.group());
                }
                allEdges.add(new Edge<String>(cost, findVertex(v1, allVertices), findVertex(v2, allVertices)));
            }

            return new Graph(allVertices, allEdges);
        }

        private Graph _graph;

        private void loadGraph(LoadGraph msg) {
            _graph = loadGraphFile(msg.fileName);
            for (int i = 0; i < _graph.getVertices().size(); i++) {
                String strAgentName = "Searcher-" + i;
                lstSearchers.add(getContext().spawn(GraphSearchAgent.create(), strAgentName));
                Graph cloneGraph = new Graph(_graph.getVertices(), _graph.getEdges());
                lstSearchers.get(i).tell(new GraphSearchAgent.ProcessGraph(cloneGraph, (Vertex<String>) cloneGraph.getVertices().get(i)));
            }
        }

        private void findRoute(FindRoute msg) {
            Graph cloneGraph = new Graph(_graph.getVertices(), _graph.getEdges());
            lstSearchers.get(cloneGraph.getVertexIndex(msg.source)).tell(new GraphSearchAgent.ReturnRoute(cloneGraph, cloneGraph.getVertex(msg.destination)));
        }

        private Behavior<GraphSearchProtocol> onReceipt(GraphSearchProtocol msgCommand) {
            if (msgCommand instanceof LoadGraph)
                loadGraph((LoadGraph) msgCommand);
            else if (msgCommand instanceof FindRoute)
                findRoute((FindRoute) msgCommand);
            return this;
        }
    }

    static void main(String[] args) {
        final ActorSystem<GraphSearchProtocol> system =
                ActorSystem.create(GraphSearchOverseer.create(), "agentOverseer");

        System.out.println("Graph Search using Dijkstra and MultiAgents");
        try {
            Scanner sc = new Scanner(new File("Graph.txt"));
            while (sc.hasNextLine()) {
                System.out.println(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        system.tell(new GraphSearchOverseer.LoadGraph("Graph.txt"));

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter Source: ");
            String source = sc.nextLine();
            System.out.println("Enter Destination: ");
            String destination = sc.nextLine();

            system.tell(new GraphSearchOverseer.FindRoute(source, destination));

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("To continue press y or x to exit");

            while (true){
                String con = sc.nextLine();
                if(con.equals("Y")||con.equals("y")){
                    break;
                }else if(con.equals("X")||con.equals("x")){
                    System.exit(1);
                }
            }
        }
    }
}



