+ Array{
    arePairs{
        ^this.size % 2 == 0
    }

    areNotPairs{
        ^this.arePairs.not
    }

    areTriplets{
        ^this.size % 3 == 0
    }

    areNotTriplets{
        ^this.areTriplets.not
    }
}
