package org.example;

import java.util.*;
import java.util.List;

import static java.lang.Math.*;

public class NumberFieldSieve {

    public static void main(String[] args) {
        NumberFieldSieve numberFieldSieve = new NumberFieldSieve();
        System.out.println(numberFieldSieve.factorize(126));
        System.out.println(numberFieldSieve.factorize(2147483646));


    }

    public int mod(int number, int mod) {
        return ((number % mod) + mod) % mod;
    }

    public int getSieveValue(int offset, int sieveBase, int number) {
        int sum = offset + sieveBase;
        return sum * sum - number;
    }


    public int getDiscriminant(int offset, int multiplier, int sieveBase, int number) {
        return (offset + sieveBase * multiplier) * (offset + sieveBase * multiplier) - number * (multiplier * multiplier);
    }

    public int factorize(int number) {
        int sieveBase = (int) Math.floor(sqrt(number));
        int limit = 5 * (int) Math.pow(log(number), 2);

        List<Integer> smoothNumbers = new ArrayList<>();
        List<Integer> primeNumbers = getPrimeNumbers(limit).stream().sorted().toList();
        for (Integer primeNumber : primeNumbers) {
            for (int i = 1; i <= primeNumber; i++) {
                if (mod(getSieveValue(i, sieveBase, number), primeNumber) == 0) {
                    smoothNumbers.add(primeNumber);
                    break;
                }
            }
        }

        List<Pair> pairs = new LinkedList<>();
        List<List<Integer>> exponentRows = new LinkedList<>();
        for (int i = 0; i < 2 * smoothNumbers.size(); i++) {
            for (int j = i * sieveBase - 50; j < i * sieveBase + 50; j++) {
                int residue = j - i * sieveBase;
                int discriminant = getDiscriminant(j, i, sieveBase, number);
                if (isFactorized(residue, smoothNumbers) && isFactorized(discriminant, smoothNumbers)) {
                    List<Integer> exponentRow = new ArrayList<>();
                    for (Integer exponent : getExponents(residue, smoothNumbers)) {
                        exponentRow.add(exponent % 2);
                    }
                    for (Integer exponent : getExponents(discriminant, smoothNumbers)) {
                        exponentRow.add(exponent % 2);
                    }
                    exponentRows.add(exponentRow);
                    pairs.add(new Pair(j, i));
                }
            }
        }

        if (!pairs.isEmpty() && !exponentRows.isEmpty()) {
            for (List<Pair> linearDependentRow : getLinearDependentRows(pairs, exponentRows)) {
                int baseProduct = 1;
                int sieveProduct = 1;

                if (!linearDependentRow.isEmpty()) {
                    for (Pair pair : linearDependentRow) {
                        baseProduct *= pair.key() - pair.value() * sieveBase;
                        sieveProduct *= getDiscriminant(pair.key(), pair.value(), sieveBase, number);
                    }

                    if (Math.sqrt(sieveProduct) == Math.floor(Math.sqrt(sieveProduct))) {
                        int gcd = gcd(Math.abs(baseProduct - sieveProduct), number);
                        if (gcd != 1 && gcd != number) {
                            return gcd;
                        }
                    }
                }
            }
        }

        throw new RuntimeException(number + " can't be factorized");
    }

    public int gcd(int dividend, int divisor) {
        if (divisor == 0) {
            return dividend;
        }

        return gcd(divisor, dividend % divisor);
    }

    public List<List<Pair>> getLinearDependentRows(List<Pair> pairs, List<List<Integer>> exponentRows) {
        List<List<Pair>> dependentRowIndices = new ArrayList<>();
        int rowSize = exponentRows.size();
        int row = 0;
        while (row < rowSize) {
            dependentRowIndices.add(new ArrayList<>(List.of(pairs.get(row))));
            row++;
        }

        int columnSize = exponentRows.get(0).size();
        for (int i = 0; i < rowSize && i < columnSize; i++) {
            int mainElement = exponentRows.get(i).get(i);
            if (mainElement == 0) {
                int l = 1;
                while (mainElement == 0 && l < rowSize) {
                    mainElement = exponentRows.get(l).get(i);
                    l++;
                }
                if (mainElement != 0) {
                    exchangeRows(exponentRows, i, l - 1);
                    exchangeRows(dependentRowIndices, i, l - 1);
                    exchange(pairs, i, l - 1);
                }
            }
            if (mainElement != 0) {
                for (int j = 0; j < rowSize; j++) {
                    if (i != j && exponentRows.get(j).get(i) != 0) {
                        for (int k = 0; k < columnSize; k++) {
                            exponentRows.get(j).set(k, mod(exponentRows.get(j).get(k) - exponentRows.get(i).get(k), 2));
                        }
                        dependentRowIndices.get(j).add(pairs.get(i));
                    }
                }
            }
        }

        List<List<Pair>> zeroExponentRows = new ArrayList<>();
        for (int i = 0; i < exponentRows.size(); i++) {
            boolean isLinearDependent = true;
            for (Integer exponent : exponentRows.get(i)) {
                if (exponent != 0) {
                    isLinearDependent = false;
                    break;
                }
            }
            if (isLinearDependent) {
                zeroExponentRows.add(dependentRowIndices.get(i));
            }
        }

        return zeroExponentRows;
    }

    public <T> void exchangeRows(List<List<T>> matrix, int i, int j) {
        if (i == j) return;
        List<T> row = matrix.get(i);
        matrix.set(i, matrix.get(j));
        matrix.set(j, row);

    }

    public <T> void exchange(List<T> row, int i, int j) {
        if (i == j) return;
        T e = row.get(i);
        row.set(i, row.get(j));
        row.set(j, e);
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

    public List<Integer> getExponents(int number, List<Integer> smoothNumbers) {
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