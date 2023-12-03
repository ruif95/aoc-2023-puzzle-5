import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static final String WRONG_INPUT_FILE_MESSAGE = "Wrong file";
    public static final String INPUT_FILENAME = "input";

    public static void main(String[] args) throws IOException {
        final List<String> inputLines = extractInputLines();

        final char[][] schematicMatrix = toMatrix(inputLines);
        final List<MatrixNumber> matrixNumbers = getMatrixNumbers(schematicMatrix);
        final List<MatrixNumber> symbolAdjacentNumbers = matrixNumbers.stream()
                .filter(matrixNumber -> matrixNumber.isSymbolAdjacent(schematicMatrix))
                .toList();

        int result = symbolAdjacentNumbers.stream()
                .mapToInt(MatrixNumber::getNumber)
                .sum();

        System.out.println("A-ha! The number to help the missing part is: " + result);
    }

    private static char[][] toMatrix(final List<String> inputLines) {
        final int inputWidth = inputLines.get(0).length();
        final int inputHeight = inputLines.size();
        char[][] matrix = new char[inputWidth][inputHeight];

        for (int j = 0; j < inputHeight; j++) {
            matrix[j] = inputLines.get(j).toCharArray();
        }

        return matrix;
    }

    private static List<MatrixNumber> getMatrixNumbers(char[][] matrix) {
        List<MatrixNumber> result = new ArrayList<>();

        MatrixNumber currentMatrixNumber = new MatrixNumber();
        boolean currentlyCreatingANumber = false;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (j == 0 && currentlyCreatingANumber) {
                    currentMatrixNumber.setNumber();
                    result.add(MatrixNumber.copyOf(currentMatrixNumber));
                    currentlyCreatingANumber = false;
                }

                if (Character.isDigit(matrix[i][j])) {
                    if (!currentlyCreatingANumber) {
                        currentMatrixNumber = new MatrixNumber();
                        currentlyCreatingANumber = true;
                    }

                    currentMatrixNumber.addDigit(Character.getNumericValue(matrix[i][j]));
                    currentMatrixNumber.addIndex(i, j);
                } else {
                    if (currentlyCreatingANumber) {
                        currentMatrixNumber.setNumber();
                        result.add(MatrixNumber.copyOf(currentMatrixNumber));
                        currentlyCreatingANumber = false;
                    }
                }
            }
        }

        return result;
    }

    private static boolean isSymbol(char character) {
        return '.' != character && !Character.isDigit(character) && !Character.isLetter(character) && !Character.isWhitespace(character);
    }

    private static List<String> extractInputLines() throws IOException {
        try (InputStream resource = Main.class.getResourceAsStream(INPUT_FILENAME)) {
            if (resource == null) {
                throw new RuntimeException(WRONG_INPUT_FILE_MESSAGE);
            }

            return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .lines()
                    .toList();
        }
    }

    static class MatrixNumber {
        Integer number;
        List<Integer> digits;
        List<Index> indexes;

        public MatrixNumber() {
            this.number = 0;
            this.digits = new ArrayList<>();
            this.indexes = new ArrayList<>();
        }

        public static MatrixNumber copyOf(MatrixNumber matrixNumber) {
            return new MatrixNumber(matrixNumber.getNumber(),
                    matrixNumber.getDigits(),
                    matrixNumber.getIndexes());
        }

        private MatrixNumber(Integer number, List<Integer> digits, List<Index> indexes) {
            this.number = number;
            this.digits = digits;
            this.indexes = indexes;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber() {
            this.number = Integer.parseInt(digits.stream().map(Object::toString).collect(Collectors.joining()));
        }

        public List<Integer> getDigits() {
            return digits;
        }

        public void addDigit(Integer digit) {
            this.digits.add(digit);
        }

        public List<Index> getIndexes() {
            return indexes;
        }

        public void addIndex(int i, int j) {
            this.indexes.add(new Index(i, j));
        }

        public boolean isSymbolAdjacent(char[][] matrix) {
            int leftmostI = Collections.min(indexes.stream().map(Index::i).toList()) - 1;
            int rightmostI = Collections.max(indexes.stream().map(Index::i).toList()) + 1;
            int downmostJ = Collections.min(indexes.stream().map(Index::j).toList()) - 1;
            int upmostJ = Collections.max(indexes.stream().map(Index::j).toList()) + 1;

            for (int i = leftmostI; i <= rightmostI; i++) {
                for (int j = downmostJ; j <= upmostJ; j++) {
                    if (i < 0 || i >= matrix.length || j < 0 || j >= matrix[i].length) {
                        continue;
                    }

                    if (isSymbol(matrix[i][j])) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    record Index(int i, int j) { }
}
