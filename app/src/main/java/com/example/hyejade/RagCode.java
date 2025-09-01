package com.example.hyejade;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channels;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.GraphStateException;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;

import com.example.hyejade.embed.MediaPipeTextEmbedderImpl;
import com.example.hyejade.embed.RagTextEmbedder;
import com.example.hyejade.vec.ChunkIndexer;
import com.example.hyejade.vec.Retriever;

import java.util.*;
import java.util.function.Supplier;
//      static String TAG_NAME = "GreeterNode";
//
//      LoggerUtils logger = new LoggerUtils().getInstance();
//      logger.debug(TAG_NAME, state.messages().toString());


// Define the state for our graph
class GraphState extends AgentState {
    public static final String QUERY_KEY = "query";
    public static final String RETRIEVER_KEY = "retriever";
    public static final String ANSWER_KEY = "answer";
    public static final String MESSAGES_KEY = "messages";
    public static final String NEEDS_TOOL_KEY = "needs_tool";
    public static final String ANSWER_CHECK_KEY = "false";


    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            // MESSAGES_KEY, Channels.appender(ArrayList::new),
            QUERY_KEY, Channels.base(
                    new Supplier<String>() {
                        @Override
                        public String get() { return "";  }
                    }),

            RETRIEVER_KEY, Channels.base(
                    new Supplier<List<Retriever.Hit>>() {
                        @Override
                        public List<Retriever.Hit> get() { return new ArrayList<>(); }
                    }
            ),

            ANSWER_KEY, Channels.base(
                    new Supplier<String>() {
                        @Override
                        public String get() { return "";  }
                    }),

            MESSAGES_KEY, Channels.appender(
                    new Supplier<List<String>>() {
                        @Override
                        public ArrayList<String> get() { return new ArrayList<>(); }
                    }),

            NEEDS_TOOL_KEY, Channels.base(
                    new Supplier<String>() {
                        @Override
                        public String get() { return "";  }
                    }),
            ANSWER_CHECK_KEY, Channels.base(
                    new Supplier<String>(){
                        @Override
                        public String get() {return "false";}
                    }
            )
    );

    // initData 하는 부분
    public GraphState(Map<String, Object> initData) {
        super(initData);
    }

    // 실제 호출시 내부 데이터 를 주는 부분
    public String query() {
        return this.<String>value(QUERY_KEY).orElse("");
    }

    public List<Retriever.Hit> retriever() {
        return this.<List<Retriever.Hit>>value(RETRIEVER_KEY).orElseGet(ArrayList::new);
    }

    public String answer() {
        return this.<String>value(ANSWER_KEY).orElse("");
    }

    public List<String> messages() {
        return this.<List<String>>value(MESSAGES_KEY).orElse(List.of());
    }

    public String needs_tool() {
        return this.<String>value(NEEDS_TOOL_KEY).orElse("");
    }
    public String answer_check(){
        return this.<String>value(ANSWER_CHECK_KEY).orElse("false");
    }
}
// Node that adds a RetrieveNode
class RetrieveNode implements NodeAction<GraphState> {
    private final RagTextEmbedder embedder;
    private final Retriever vecRetriever;
    private final int topK;

    public RetrieveNode(RagTextEmbedder embedder, Retriever vecRetriever, int topK) {
        this.embedder = embedder;
        this.vecRetriever = vecRetriever;
        this.topK = topK;
    }

    @Override
    public Map<String, Object> apply(GraphState state) {
        final String q = state.query();
        if (q == null || q.isBlank()) {
            return Map.of(GraphState.RETRIEVER_KEY, Collections.<Retriever.Hit>emptyList()); // ✅ 리스트로 반환
        }
        float[] qvec = embedder.embed(q);
        List<Retriever.Hit> hits = vecRetriever.topK(qvec, topK);
        return Map.of(GraphState.RETRIEVER_KEY, hits);
    }
}

// Node that adds a AnswerNode
class AnswerNode implements NodeAction<GraphState> {
    @Override
    public Map<String, Object> apply(GraphState state) {
        List<Retriever.Hit> hits = state.retriever();
        String query = state.query();

        StringBuilder ctx = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            var h = hits.get(i);
            ctx.append(String.format("- #%d dist=%.4f %s[%d]%n%s%n%n", i + 1, h.distance, h.docId, h.chunkIndex, h.text.trim()));
        }

        // 데모용 간단 답변: 컨텍스트 요약(실전에서는 LLM 호출로 대체)
        String draft = "Q: " + query + "\n\nTop-K Contexts:\n" + ctx +
                "Draft Answer: 컨텍스트를 참고해 생성 모델을 호출하거나 규칙 기반으로 답을 구성하세요.";
        return Map.of(GraphState.ANSWER_KEY, draft);
    }
}

class ConditionNode implements NodeAction<GraphState> {
    @Override
    public Map<String, Object> apply(GraphState state) {
        String answer = state.answer();
        String flag = (answer != null && !answer.isBlank()) ? "true" : "false";
        return Map.of(GraphState.ANSWER_CHECK_KEY, flag);
    }
}

class EndNode implements NodeAction<GraphState> {
    @Override
    public Map<String, Object> apply(GraphState state) {
        String check = state.answer_check();
        if ("true".equals(check)) {
            System.out.println("=== FINAL ANSWER ===");
            System.out.println(state.answer());
        } else {
            System.out.println("Answer not generated.");
        }
        return Map.of(GraphState.ANSWER_CHECK_KEY, "done");
    }
}

public class RagCode {

    public static void main(String[] args) throws GraphStateException {
        final String MODEL_ASSET = "text_embedder.tflite"; // assets에 존재해야 함
        final int    TOP_K       = 5;

        try (RagTextEmbedder embedder =
                     new MediaPipeTextEmbedderImpl(App.context(), MODEL_ASSET,
                             /*l2Normalize=*/false, /*quantize=*/false)) {

            // (선택) 임베딩 차원 확인 로그
            System.out.println("[Embedder] dims = " + embedder.dimensions());

            // ===== (데모) 인덱싱 =====
            ChunkIndexer indexer = new ChunkIndexer(embedder);

            String doc1 = """
                    브레이크 패드는 마모 정도에 따라 교체 주기가 달라집니다.
                    일반적으로 3만~5만 km에서 점검 및 교체를 권장합니다.
                    소음, 제동거리 증가, 페달 감각 변화가 느껴지면 점검하세요.
                    """;
            String doc2 = """
                    타이어 공기압은 계절과 주행 환경에 따라 변동합니다.
                    TPMS 경고등이 들어오면 공기압 점검이 필요합니다.
                    고속주행 전에는 제조사 권장 압력을 맞추는 것이 안전합니다.
                    """;
            // 청크 크기/중첩은 데이터 특성에 맞춰 조절
            indexer.indexDocument("manual/brake", doc1, 220, 40);
            indexer.indexDocument("manual/tire",  doc2, 220, 40);

            // ===== Retriever 준비 =====
            Retriever vecRetriever = new Retriever();

            // ===== 노드 구성 =====
            RetrieveNode retrieveNode = new RetrieveNode(embedder, vecRetriever, TOP_K);
            AnswerNode   answerNode   = new AnswerNode();
            ConditionNode conditionNode = new ConditionNode();
            EndNode      endNode      = new EndNode();

            // ===== 그래프 정의 =====
            var stateGraph = new StateGraph<>(GraphState.SCHEMA, GraphState::new)
                    .addNode("retrieve",  node_async(retrieveNode))
                    .addNode("answer",    node_async(answerNode))
                    .addNode("condition", node_async(conditionNode))
                    .addNode("end",       node_async(endNode))
                    .addEdge(START, "retrieve")
                    .addEdge("retrieve", "answer")
                    .addEdge("answer", "condition")
                    .addEdge("condition", "end")
                    .addEdge("end", END);

            var compiledGraph = stateGraph.compile();

            // ===== 실행 =====
            Map<String, Object> initialState = Map.of(
                    GraphState.QUERY_KEY, "브레이크 패드 교체 주기와 증상"
            );

            compiledGraph.invoke(initialState)
                    .ifPresent(finalState ->
                            System.out.println("Final answer check: " + finalState.answer_check())
                    );

        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof GraphStateException gse) {
                throw gse;
            } else {
                GraphStateException wrap =
                        new GraphStateException(e.getMessage() != null ? e.getMessage() : e.toString());
                wrap.initCause(e); // ✅ cause 연결
                throw wrap;
            }
        }
    }
}

