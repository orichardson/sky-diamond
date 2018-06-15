package diagrams.algebra

object Combinatorics {


  /** Initial part of Pascal's Triangle, precomputed to speed calculations
    *  (Binomial Coefficients)
    */
  val pascalTri = Array (         Array (1),
    Array (1,  1),
    Array (1,  2,  1),
    Array (1,  3,  3,  1),
    Array (1,  4,  6,  4,  1),
    Array (1,  5, 10, 10,  5,  1),
    Array (1,  6, 15, 20, 15,  6,  1),
    Array (1,  7, 21, 35, 35, 21,  7,  1),
    Array (1,  8, 28, 56, 70, 56, 28,  8,  1),
    Array (1,  9, 36, 84,126,126, 84, 36,  9,  1),
    Array (1, 10, 45,120,210,252,210,120, 45, 10,  1),
    Array (1, 11, 55,165,330,462,462,330,165, 55, 11,  1),
    Array (1, 12, 66,220,495,792,924,792,495,220, 66, 12,  1),
    Array (1, 13,78,286,715,1287,1716,1716,1287,715,286,78,13, 1),
    Array (1,14,91,364,1001,2002,3003,3432,3003,2002,1001,364,91,14,1),
    Array (1,15,105,455,1365,3003,5005,6435,6435,5005,3003,1365,455,105,15,1))

  def choose (n: Int, k: Int): Long = {
    if (k == 0 || k == n) 1l // 1L, not eleven.
    else if (n < pascalTri.length) pascalTri(n)(k)
    else choose (n-1, k-1) + choose (n-1, k)
  }

}
