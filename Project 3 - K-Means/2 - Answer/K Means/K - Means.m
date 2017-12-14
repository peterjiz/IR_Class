K-MEANS (set of N document vectors x1 to xN, K)
{
    retrieve set of K document vectors S1 to Sk ← SELECT RANDOM SEEDS(from x1 to xN, K)
    
    //Assign seeds to centroids
    for k = 1 to K
    {
        //Assign K Seed Vectors to uk centroids
        vector uk = vector sk //centroid is equal seed
    }
    
    //As long as we're not satisfied = we haven't stopped
    while stopping criterion has not been met
    {
        //Create new empty cluster k for all seeds k (or not formed yet clusters)
        //Create K clusters
        for k = 1 to K
        {
            ωk = {}
        }
        
        //For all document vectors
        for n = 1 to N
        {
            //Find the closest centroid to document vector n
            j = argminj′ |⃗μj′ − ⃗xn|
            
            //Get the cluster closest to document n AND add document n to it
            ω(k = j) = ω(k = j) ∪ {⃗xn} (reassignment of vectors)
        }
        
        //For all K clusters, recompute the new centroid
        for k = 1 to K
        {
            ⃗μk = 1/|wk| * ∑⃗x∈ω ⃗x (recomputation of centroids)
        }
    }
    //return the set of centroids
    return {⃗μ1,...,⃗μK}
}