#!! FLOW OF PROGRAM

You enter the workspace. Now what?

PHASE 0 : Choose Geometry
	trait Geometry {
		type Dimension

		type Dir
		type Scalar: = sig {
			// Ring, but also needs additional information:
			// capture the "splitting off 1" of natural numbers,
			// and the "divide into k" of real numbers.
			// Should be able to create an abstract scalar for any given type.

			views : List[Scalar => List[Scalar]] ->
		}



		>> type Vec = (Dir, Scalar)

		def + (Pos, Vec) -> Pos
		def * (Vec, Scalar) -> Vec
		def = (Pos, Pos) -> Bool
		def = (Vec, Vec) -> Bool

		// def - (Vec) -> Vec

	}

	Examples:
		2D:
			⎔  ⇔ ◁  	hex and triangular lattices are dual
			ℝⁿ ⇔ ℝⁿ 	real space self dual
			□  ⇔ □	 	 Square latices are also self-dual

		3D:

α

PHASE 1 : create an n-cell of any kind
	create points (positions? kinda?)
	connect cells together

	OR: use helper methods.
	Display?


PHASE 2: Choose an invariant

	- length, area, volume
	- dual weight
	- topology

PHASE 3: apply invariant operations

Result: an (n+1) cell with an invariant n-dimension.


# INTERFACE

Tools:
	Standard Space:
		Creation:
			- Create point at mouse, [auto name, type key first before clicking]
		 	- drag to merge n-cells with same recursive decomposition
			- Extrude
			- Draw
			- Infer from image

		Destruction:
			- Delete n-cell
			- cut between points (! have to prove validity!)

	Dual Space
		Creation:
			- Make node with any balanced condition, representing convex polytope
			- Glue along lines

		Destruction
			- split
