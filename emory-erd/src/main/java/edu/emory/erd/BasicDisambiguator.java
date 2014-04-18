package edu.emory.erd;

import edu.emory.erd.types.Annotation;
import edu.emory.erd.types.AnnotationSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Simple disambiguator, that keeps most frequent alternative for overlapping mentions.
 */
public class BasicDisambiguator implements Disambiguator {

    private void reconstructSolution(List<Annotation> mentions, List<HashMap<Integer, Boolean>> path,
                                     List<Annotation> res) {
        int currentBoundary = -1;
        for (int i = 0; i < mentions.size(); ++i) {
            if (path.get(i).get(currentBoundary)) {
                res.add(mentions.get(i));
                currentBoundary = mentions.get(i).getSpan().getEnd();
            }
        }
    }

    /**
     * Calculates optimal combination of mentions to keep based on total sum of scores.
     * @param mentions Initial list of mentions.
     * @param currentPos Mention we are currently considering (we will move recursively).
     * @param currentBoundary Current maximum right boundary to detect intersections.
     * @param res Resulting list of non-intersecting mentions.
     * @param mem HashMap for memoization.
     * @param path HashMap to store optimal path to reconstruct the answer.
     */
    private double removeIntersecting(List<Annotation> mentions, int currentPos, int currentBoundary,
                                      List<Annotation> res, List<HashMap<Integer, Double>> mem,
                                      List<HashMap<Integer, Boolean>> path) {
        if (currentPos >= mentions.size()) return 0;
        if (mem.get(currentPos).containsKey(currentBoundary)) return mem.get(currentPos).get(currentBoundary);

        // If we can include this mention.
        double scoreKeep = 0.0;
        double scoreRemove = 0.0;
        if (mentions.get(currentPos).getSpan().getStart() > currentBoundary) {
            scoreKeep = removeIntersecting(mentions, currentPos + 1, mentions.get(currentPos).getSpan().getEnd(),
                    res, mem, path) + mentions.get(currentPos).getScore();
        }
        scoreRemove = removeIntersecting(mentions, currentPos + 1, currentBoundary, res, mem, path);
        if (scoreKeep > scoreRemove) {
            path.get(currentPos).put(currentBoundary, true);
            mem.get(currentPos).put(currentBoundary, scoreKeep);
            if (currentPos == 0)
                reconstructSolution(mentions, path, res);
            return scoreKeep;
        } else {
            path.get(currentPos).put(currentBoundary, false);
            mem.get(currentPos).put(currentBoundary, scoreRemove);
            if (currentPos == 0)
                reconstructSolution(mentions, path, res);
            return scoreRemove;
        }
    }

    @Override
    public List<AnnotationSet> disambiguate(List<Annotation> mentions) {
        // First step: if a mention covers some other mentions, keep only the largest.
        List<Annotation> filteredMentions = new ArrayList<Annotation>();
        for (Annotation mention : mentions) {
            // Remove all spans that are covered by the current span.
            for (int j = filteredMentions.size() - 1; j >= 0; --j) {
                if (mention.getSpan().contains(filteredMentions.get(j).getSpan())) {
                    filteredMentions.remove(j);
                }
            }
            // If the current spam is not covered by existing spans, add it.
            if (filteredMentions.size() == 0 ||
                    !filteredMentions.get(filteredMentions.size()-1).getSpan().contains(mention.getSpan())) {
                filteredMentions.add(mention);
            }
        }
        List<Annotation> results = new ArrayList<Annotation>();
        List<HashMap<Integer, Double>> mem = new ArrayList<HashMap<Integer, Double>>(filteredMentions.size());
        List<HashMap<Integer, Boolean>> path = new ArrayList<HashMap<Integer, Boolean>>(filteredMentions.size());
        for (int i = 0; i < filteredMentions.size(); ++i) {
            mem.add(new HashMap<Integer, Double>());
            path.add(new HashMap<Integer, Boolean>());
        }
        removeIntersecting(filteredMentions, 0, -1, results, mem, path);
        List<AnnotationSet> res = new ArrayList<AnnotationSet>();
        res.add(new AnnotationSet(0));
        for (Annotation annotation : results) {
            res.get(0).addAnnotation(annotation);
        }
        return res;
    }
}
