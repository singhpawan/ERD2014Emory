package edu.emory.erd.types;

import opennlp.tools.util.Span;

/**
 * Represents entity annotation.
 */
final public class Annotation implements Comparable<Annotation> {
    // Text of the whole document that is being annotated
    private final Text sourceText;
    // Annotated span
    private final Span span;
    // Entity the span is annotated with
    private final EntityInfo entityInfo;
    // Confidence score for the entity annotation.
    private final double score;

    public Annotation(Text docText, Span span, EntityInfo info, double score) throws IllegalArgumentException {
        sourceText = docText;
        this.span = span;
        entityInfo = info;
        this.score = score;
    }

    /** Returns the text of the annotated mention
     * It takes substring every time it is called.
     *  */
    public String getMentionText() {
        return sourceText.getSpanText(span);
    }

    /** Get information about the entity */
    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    /**
     * Returns source document to which the annotated span belongs.
     * @return A text of the document as Text.
     */
    public Text getDocumentText() {
        return sourceText;
    }

    /**
     * Returns a Span object for annotated text.
     * @return An object of class Span which tells us which interval in the document was annotated.
     */
    public Span getSpan() {
        return span;
    }

    /**
     * Returns the confidence score for the current annotation.
     * @return Annotation confidence score (from 0 to 1).
     */
    public double getScore() {
        return this.score;
    }

    /**
     * Annotations are compared by their span.
     * @param annotation Annotation to compare to.
     * @return result of comparison (negative, 0 or positive).
     */
    @Override
    public int compareTo(Annotation annotation) {
        return span.compareTo(annotation.span);
    }

    @Override
    public String toString() {
        return this.entityInfo + "\t" + this.getMentionText() + "\t" + this.getScore();
    }
}
