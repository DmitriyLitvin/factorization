package org.example;


import static java.lang.Math.log;

public class Lenstra {

    public static void main(String[] args) {
        Lenstra lenstra = new Lenstra();
        System.out.println(lenstra.factorizeNumber(1573344559));
    }

    public int getFactorial(int n) {
        int factorial = 1;
        for (int i = 1; i <= n; i++) {
            factorial *= i;
        }

        return factorial;
    }

    public int factorizeNumber(int number) {
        int limit = (int) Math.exp(Math.sqrt(2 * log(number) * log(log(number))));

        int qtyOfCurves = 0;
        int a = 3;
        int b = 5;
        while (qtyOfCurves < 1000) {
            int x = 1;
            int y = 1;
            int i = 2;
            int j = 2;
            int p;
            do {
                p = getFactorial(i);
                int x1 = x;
                int y1 = y;
                int x2 = 0;
                int y2 = 0;
                for (; j <= p; j *= 2) {
                    int dy = a * (int) Math.pow(x, 2) + b;
                    int dx = 2 * y;
                    int s;
                    if (dy % dx == 0) {
                        s = mod(dy / dx, number);
                    } else {
                        int gcd = gcd(dx, number);
                        if (gcd > 1) {
                            return gcd;
                        }
                        s = mod(dy * gcdExtended(dx, number)[1], number);
                    }
                    x2 = mod((int) Math.pow(s, 2) - 2 * x, number);
                    y2 = mod(s * (x - x2) - y, number);
                    x = x2;
                    y = y2;
                }
                if (2 * p != j) {
                    int dy = y2 - y1;
                    int dx = x2 - x1;

                    int s;
                    if (dy % dx == 0) {
                        s = mod(dy / dx, number);
                    } else {
                        int gcd = gcd(dx, number);
                        if (gcd > 1) {
                            return gcd;
                        }
                        s = mod(dy * gcdExtended(dx, number)[1], number);
                    }

                    x2 = mod((int) Math.pow(s, 2) - x1 - x2, number);
                    y2 = mod(s * (x1 - x2) - y1, number);
                    x = x2;
                    y = y2;
                }
                i++;
            } while (p < limit);
            a++;
            b++;
            qtyOfCurves++;
        }

        throw new RuntimeException(number + " can't be factorized");
    }


    public int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    public int[] gcdExtended(int a, int b) {
        int res[] = new int[3];
        if (b == 0) {
            res[0] = a;
            res[1] = 1;
            res[2] = 0;
            return res;

        }
        res = gcdExtended(b, a % b);
        int s = res[2];
        res[2] = res[1] - (a / b) * res[2];
        res[1] = s;

        return res;
    }


    public int mod(int number, int mod) {
        return ((number % mod) + mod) % mod;
    }
}
