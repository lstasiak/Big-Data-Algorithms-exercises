# Big-Data-Algorithms-exercises
Set of tasks solved in Big Data Algorithms course at WUST
The main literature regarding this course include: "Mining of massive datasets" by J. Leskovec, A. Rajaraman and J. Ullman.

## Problem 1 - Jaccard Distance & Minhashing

### **Task A: Problem description**
Write the procedure with the interface `jaccard(f1:String,f2:String,k:Integer):Double` which for the files named `f1` and `f2` determines their k-shingles and then calculates their Jaccard distance. Before determining k-shingles, the files should be cleaned (the minimum is to delete new line characters, tabs and double spaces).

### **Task B: Problem description**
Apply the min-hash method to the previous problem (Problem A). Your procedure should depend on the H parameter, which determines the number of hash functions used to build the signature. Test this procedure on the data from the previous problem for H ∈ {50, 100, 250} and compare the Jaccard distance approximation with its exact values. Remember to generate a shared family of hash functions for all analyzed texts.

### **Quick info**
Jaccard Similarity is the measure of similarity of objects defined as:

![formula](https://latex.codecogs.com/svg.latex?J_%7Bsim%7D%20%3D%20%5Cfrac%7B%7CA%5Ccap%20B%7C%7D%7B%7CA%5Ccup%20B%7C%7D)

where A and B are sets of objects. Jaccard Distance is simply defined as 1 - J_SIM.

#### What is the **k-shingles**?
"Shingling" in language processing and data mining is extracting set of sub-strings (everyone has length k) from a given string/sequence. 
Example. Given a sequence: `"Hello, world!"`, the 3-shingle set is defined as: `{"Hel", "ell", "llo", "low", "owo", "wor", "orl", "rld"}` (after removing stopwords, blank spaces and punctuation).

#### Minhashing
The idea of comparing large sets of objects using **signatures**. A signature is a <smaller> set representing the main dataset (e.g. a document). The signature has to be a more optimal way of representing large set of objects, than k-shingles method using family of hash functions. One of the method of building signatures is a method of characteristic matrix (but not so efficient if using random permutations).
_____
more soon...

