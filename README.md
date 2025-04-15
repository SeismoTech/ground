Ground: General ROUtiNes Definition
======================================================================

Code shared across several projects of Seismo Technologies.

We are not encouraging direct use of this code;
probably, most of it can be found in other huge swiss-knife projects
like Guava or Apache Common.
We are making this open only because other more interesting projects are
using it (for instance, [Hashish](https://github.com/SeismoTech/hashish)).

## Versioning

To simplify dependency management, this project is evolved as a rolling
version project, where all changes are always backward compatible.
Therefore, it will be forever at 1.x.y version, and mostly at 1.x.0;
major version will never change because that would mean an incompatible change;
there is no need to move patch version, because any bugfix can be
seen as a new compatible enhancement.
