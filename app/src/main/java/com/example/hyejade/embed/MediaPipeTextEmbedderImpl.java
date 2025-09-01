package com.example.hyejade.embed;

import android.content.Context;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder;
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions;
import com.google.mediapipe.tasks.text.textembedder.TextEmbedderResult;
import com.google.mediapipe.tasks.components.containers.Embedding;
import java.util.List;

public class MediaPipeTextEmbedderImpl implements RagTextEmbedder, AutoCloseable {
    private final TextEmbedder mp;
    private final int dims;

    public MediaPipeTextEmbedderImpl(Context context, String assetModelPath,
                                     boolean l2Normalize, boolean quantize) {
        BaseOptions base = BaseOptions.builder()
                .setModelAssetPath(assetModelPath)
                .build();

        TextEmbedderOptions opts = TextEmbedderOptions.builder()
                .setBaseOptions(base)
                .setL2Normalize(l2Normalize)
                .setQuantize(quantize)
                .build();

        this.mp = TextEmbedder.createFromOptions(context, opts);

        // 임베딩 차원 확인
        TextEmbedderResult r = this.mp.embed("dimension check");
        List<Embedding> list = r.embeddingResult().embeddings();
        if (list.isEmpty()) throw new IllegalStateException("No embeddings returned");
        float[] vec = list.get(0).floatEmbedding();
        this.dims = vec.length;
    }

    @Override public int dimensions() { return dims; }

    @Override public float[] embed(String text) {
        if (text == null) text = "";
        TextEmbedderResult r = mp.embed(text);
        List<Embedding> list = r.embeddingResult().embeddings();
        if (list.isEmpty()) return new float[0];
        return list.get(0).floatEmbedding();
    }

    @Override public void close() { mp.close(); }
}
