##WordCount
* Api to be used by client is ```WordCount```
* ```FixedBatchSpliterator``` is generic and can be used with any other streams api. The implementation is inspired from discussion between authors of Java on the [following](http://markmail.org/message/xgcm4dlyrkjymrw4) thread. 
    * The main reasoning behind the implementation is to utilize concurrency in an efficient manner if the stream size is unknown. Stream api uses a spliterator that that relies on the fact that stream size is known in advance. When this isn't true, the jdk impl can be made more efficient, which is what I do.
    * Also, the efficiency gained can also be attributed to the fact that if after chunking the computation in parallel, the chunk of computation itself is small, then the cost in terms of context switching etc. and other overhead of threads isn't justified and it results in inefficiency. Thus we want to ensure that each thread is given significant computation to overcome overhead costs and batching allows that.
    * The approach is straight forward, when we don't know the size of the stream, we batch it up and since each batch has a fixed size, we can now rely on the efficient impl of jdk.
      

##Assumptions
* While counting frequency I make certain assumptions since there wasn't any clear direction.
    1. All words are case sensitive. i.e I count 'you' and 'You' are different words. (It is trivial to make then case insensitive)
    2. "can't" is taken as 1 word. Also, 'Delaney-Podmore' is considered to be 1 word. Trailing and prefix ' and - will be ignored' and so will be any other non alphanumeric. Thus, 'yes-' will be parsed to be only 'yes'. Other scenarios are documented via test cases in WordCount#"test countFrequency()". 
    3. If the requirements are different it can be easily accommodated by using a different regex when creating WordCount instance (A constructor is provided for the same).  
    4. In the worst case, count of a specific word is in the range of long.
    5. I ignore , . ? etc symbols. To consider as valid words I only count a-z, 0-9 and ' or - if its between a-z or/and 0-9.
    6. As long as above condition is satisfied, I don't check if its a valid dictionary word or not
    7. Although, I do assume that the entire file can fit in the memory, this assumption is only required in worst case scenario. Typical distribution of words would have many repeating words and thus number of words required to be stored in practice will be much less. Streams are lazy and thus at a time we only process Code can be easily modified to not rely on this assumption with small change. 

##Time and space complexity:

####Time Complexity:
1. The main processing happens in WordCountDelegate.countFrequency() and WordCountDelegate.sortMapByValue()
    * WordCountDelegate.countFrequency() : This involves 2 steps. 
        1. Reading the file and parsing it words as per requirements. This involves regex parsing. The library I use uses implementation of regex parsing which has time complexity of __O(n)__ and space complexity of __O(1)__. I chose to use this library since java regex libary uses backtracking algorithm for regex parsing which is exponential in worst case. google's r2j lib uses Thompson's Thompson's non definite automation NFA ([more details](https://swtch.com/~rsc/regexp/regexp1.html)). 
        2. The sorting relies on java's native sorting which has __O(mlog(m))__ worst case time complexity. Space complexity is __O(m)__ where __m__ are number of non repeating words from the file which satisfy out parsing requirements.
        3. The overall worst case time complexity __O(nlog(n))__ where n are all the words in file. This will happen when all the words in file satisfy our parsing requirements and are unique.
        4. If the distribution of words in file is more skewed, i.e. Many words repeat, then the performance will improve and will depend on non repeating words.  
        
        
        
2. I don't assume that I need to fit entire file. Although, in worst case where all words in file are non-repeating, in that case the algorithm does hold all words in memory i.e. __O(n)__. The more repetition, the less memory consumed.


##Further improvements:
The algorithm, follows map-reduce paradigm. Thus it can be scaled over multiple nodes with little change in the code. Each can be given buckets of words it needs to worry about. A simple allocation could be for e.g. Say, If we have 2 node's, we can say Node1 will operate on all words starting from 'a' to 'm' and '0' to '4' and Node2 will operate on 'n' to 'z' and '5' to '9'. This will allow us to use the same code with little modification and distribute it over 2 nodes.
The above distribution is naive distribution and in practice distribution keys should be decided based on information of approximate distribution of keys such that it load balances all nodes.