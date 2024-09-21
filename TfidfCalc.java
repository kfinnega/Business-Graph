import java.util.ArrayList;

public class TfidfCalc {

    public double tf(ArrayList<String> tfTable, String term) {
        double result = 0;
        for (String word : tfTable) {
            if (term.equalsIgnoreCase(word))
                result++;
        }
        return result / tfTable.size();
    }


    public double idf(FT docs, String term) {
        double n = 0;
        n = docs.getCount(term);


        return Math.log10(10000/ n);
    }


    public double tfIdf(ArrayList<String> doc, FT docs, String term) {
        return tf(doc, term) * idf(docs, term);
    }
}