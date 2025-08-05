package org.example;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.Math.*;

public class NumberFieldSieve {

    public static void main(String[] args) {
        NumberFieldSieve numberFieldSieve = new NumberFieldSieve();
        System.out.println(numberFieldSieve.factorizeNumber(1573344559));
    }

    public int mod(int number, int mod) {
        return ((number % mod) + mod) % mod;
    }

    public int f(int x, int m, int number) {
        return (int) Math.pow(x, 2) + 2 * m * x + (int) Math.pow(m, 2) - number;
    }


    public int getA(int a, int b, int m, int number) {
        return (int) Math.pow(a, 2) + 2 * m * a * b + (int) Math.pow(b, 2) * ((int) Math.pow(m, 2) - number);
    }

    public int factorizeNumber(int number) {
        int m = (int) Math.floor(sqrt(number));
        int limit = 5 * (int) Math.pow(log(number), 2);

        List<Integer> smoothNumbers = new ArrayList<>();
        List<Integer> primeNumbers = getPrimeNumbers(limit).stream().sorted().toList();
        for (Integer primeNumber : primeNumbers) {
            for (int i = 1; i <= primeNumber; i++) {
                if (mod(f(i, m, number), primeNumber) == 0) {
                    smoothNumbers.add(primeNumber);
                    break;
                }
            }
        }

        List<Pair> pairs = new LinkedList<>();
        List<List<Integer>> exponents = new LinkedList<>();
        for (int i = 0; i < 2 * smoothNumbers.size(); i++) {
            for (int j = i * m - 50; j < i * m + 50; j++) {
                int r = j - i * m;
                int a = getA(j, i, m, number);
                if (isFactorized(r, smoothNumbers) && isFactorized(a, smoothNumbers)) {
                    pairs.add(new Pair(j, i));
                    exponents.add(new ArrayList<>(Stream.concat(getExponentsOfSmoothNumbers(r, smoothNumbers).stream().map(n -> n % 2), getExponentsOfSmoothNumbers(a, smoothNumbers).stream().map(n -> n % 2)).toList()));
                }
            }
        }

        if (!pairs.isEmpty() && !exponents.isEmpty()) {
            List<List<Pair>> rows = getLinearDependentRows(pairs, exponents);
            for (List<Pair> row : rows) {
                int X = row.stream().map(p -> p.getA() - p.getB() * m).reduce((x, y) -> x * y).orElse(0);
                int Y = row.stream().map(p -> getA(p.getA(), p.getB(), m, number)).reduce((x, y) -> x * y).orElse(0);
                if (Math.sqrt(Y) == Math.floor(Math.sqrt(Y))) {
                    int gcd = gcd(Math.abs(X - Y), number);
                    if (gcd != 1 && gcd != number) {
                        return gcd;
                    }
                }
            }
        }

        throw new RuntimeException(number + " can't be factorized");
    }
    

    public int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }

        return gcd(b, a % b);
    }

    public List<List<Pair>> getLinearDependentRows(List<Pair> pairs, List<List<Integer>> exponents) {
        List<List<Pair>> linearOperations = new ArrayList<>();
        int rowSize = exponents.size();
        int row = 0;
        while (row < rowSize) {
            linearOperations.add(new ArrayList<>(List.of(pairs.get(row))));
            row++;
        }

        int columnSize = exponents.get(0).size();
        for (int i = 0; i < rowSize && i < columnSize; i++) {
            int mainElement = exponents.get(i).get(i);
            if (mainElement == 0) {
                int l = 1;
                while (mainElement == 0 && l < rowSize) {
                    mainElement = exponents.get(l).get(i);
                    l++;
                }
                if (mainElement != 0) {
                    exchangeRows(exponents, i, l - 1);
                    exchangeRows(linearOperations, i, l - 1);
                    exchangePairs(pairs, i, l - 1);
                }
            }
            if (mainElement != 0) {
                for (int j = 0; j < rowSize; j++) {
                    if (i != j && exponents.get(j).get(i) != 0) {
                        for (int k = 0; k < columnSize; k++) {
                            exponents.get(j).set(k, mod(exponents.get(j).get(k) - exponents.get(i).get(k), 2));
                        }
                        linearOperations.get(j).add(pairs.get(i));
                    }
                }
            }
        }

        List<List<Pair>> linearDependentRows = new ArrayList<>();
        for (int i = 0; i < exponents.size(); i++) {
            if (exponents.get(i).stream().allMatch(r -> r == 0)) {
                linearDependentRows.add(linearOperations.get(i));
            }
        }

        return linearDependentRows;
    }

    public <T> void exchangeRows(List<List<T>> exponentMatrix, int i, int j) {
        if (i == j) return;
        List<T> exponents = exponentMatrix.get(i);
        exponentMatrix.set(i, exponentMatrix.get(j));
        exponentMatrix.set(j, exponents);

    }

    public void exchangePairs(List<Pair> pairs, int i, int j) {
        if (i == j) return;
        Pair pair = pairs.get(i);
        pairs.set(i, pairs.get(j));
        pairs.set(j, pair);
    }

    public boolean isFactorized(int number, List<Integer> smoothNumbers) {
        if (number == 0) {
            return false;
        }

        int currentNumber = Math.abs(number);
        for (Integer smoothNumber : smoothNumbers) {
            while (currentNumber % smoothNumber == 0) {
                currentNumber = currentNumber / smoothNumber;
            }
            if (currentNumber == 1) {
                return true;
            }
        }

        return false;
    }

    public List<Integer> getExponentsOfSmoothNumbers(int number, List<Integer> smoothNumbers) {
        List<Integer> exponents = new LinkedList<>();
        int currentNumber = Math.abs(number);

        for (Integer smoothNumber : smoothNumbers) {
            int i = 0;
            while (currentNumber % smoothNumber == 0) {
                currentNumber = currentNumber / smoothNumber;
                i++;
            }
            exponents.add(i);
        }

        return exponents;
    }


    public List<Integer> getPrimeNumbers(int limit) {
        List<Boolean> flags = new ArrayList<>();
        flags.add(false);
        flags.add(false);

        for (int i = 2; i < limit; i++) {
            flags.add(true);
        }

        for (int i = 2; i < sqrt(limit); i++) {
            if (flags.get(i)) {
                for (int j = i * i; j < limit; j += i) {
                    flags.set(j, false);
                }
            }
        }

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            if (flags.get(i)) {
                numbers.add(i);
            }
        }

        return numbers;
    }
}