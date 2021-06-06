import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;

public class BTree <T, V extends Comparable<V>>{
    //B+ tree order
    private Integer bTreeOrder;
    //The number of children owned by the non-leaf node of the B+ tree (also the minimum number of keys)
    //private Integer minNUmber;
    //The number of nodes that the non-leaf node of the B+ tree has at its maximum (and also the maximum number of keys)
    private Integer maxNumber;

    private Node<T, V> root;

    private LeafNode<T, V> left;

    public BTree(){
        this(3);
    }

    public BTree(Integer bTreeOrder){
        this.bTreeOrder = bTreeOrder;
        //this.minNUmber = (int) Math.ceil(1.0 * bTreeOrder / 2.0);
        
        this.maxNumber = bTreeOrder + 1;
        this.root = new LeafNode<T, V>();
        this.left = null;
    }

    //Inquire
    public T find(V key){
        T t = this.root.find(key);
        if(t == null){
            //System.out.println("Not Found Content For Key " + key);
        }
        else{
            //System.out.println("Found Content For Key " + key);
        }
        return t;
    }

    //insert
    public void insert(T value, V key){
        if(key == null)
            return;
        Node<T, V> t = this.root.insert(value, key);
        if(t != null)
            this.root = t;
        this.left = (LeafNode<T, V>)this.root.refreshLeft();

// System.out.println("Insert completed, the current root node is:");
//        for(int j = 0; j < this.root.number; j++) {
//            System.out.print((V) this.root.keys[j] + " ");
//        }
//        System.out.println();
    }


    /**
           * Node parent class, because in the B+ tree, non-leaf nodes do not need to store specific data, just need to use the index as a key.
           * So the leaves and non-leaf nodes are not the same, but they share some methods, so use the Node class as the parent class.
           * And because you want to call some public methods to each other, use abstract classes
     *
           * @param <T> with BPlusTree
     * @param <V>
     */
    abstract class Node<T, V extends Comparable<V>>{
        //parent node
        protected Node<T, V> parent;
        // child node
        protected Node<T, V>[] childs;
        
        protected Integer number;
        //key
        protected Object keys[];

        //Construction method
        public Node(){
            this.keys = new Object[maxNumber];
            this.childs = new Node[maxNumber];
            this.number = 0;
            this.parent = null;
        }

        // lookup
        abstract T find(V key);

        //insert
        abstract Node<T, V> insert(T value, V key);

        abstract LeafNode<T, V> refreshLeft();
    }


    /**
           * Non-leaf node class
     * @param <T>
     * @param <V>
     */

    class BPlusNode <T, V extends Comparable<V>> extends Node<T, V>{

        public BPlusNode() {
            super();
        }

        /**
                   * Recursive lookup, here is just to determine exactly which value the value is in, the real find the leaf node will check
         * @param key
         * @return
         */
        @Override
        T find(V key) {
            int i = 0;
            while(i < this.number){
                if(key.compareTo((V) this.keys[i]) <= 0)
                    break;
                i++;
            }
            if(this.number == i)
                return null;
            return this.childs[i].find(key);
        }

        /**
                   * Recursive insertion, first insert the value into the corresponding leaf node, and finally call the insert class of the leaf node
         * @param value
         * @param key
         */
        @Override
        Node<T, V> insert(T value, V key) {
            int i = 0;
            while(i < this.number){
                if(key.compareTo((V) this.keys[i]) < 0)
                    break;
                i++;
            }
            if(key.compareTo((V) this.keys[this.number - 1]) >= 0) {
                i--;
//                if(this.childs[i].number + 1 <= bTreeOrder) {
//                    this.keys[this.number - 1] = key;
//                }
            }

// System.out.println("non-leaf node lookup key: " + this.keys[i]);

            return this.childs[i].insert(value, key);
        }

        @Override
        LeafNode<T, V> refreshLeft() {
            return this.childs[0].refreshLeft();
        }

        /**
                   * When the leaf node inserts successfully completes the decomposition, recursively inserts a new node to the parent node to maintain balance
         * @param node1
         * @param node2
         * @param key
         */
        Node<T, V> insertNode(Node<T, V> node1, Node<T, V> node2, V key){

// System.out.println("non-leaf node, insert key: " + node1.keys[node1.number - 1] + " " + node2.keys[node2.number - 1]);

            V oldKey = null;
            if(this.number > 0)
                oldKey = (V) this.keys[this.number - 1];
            
            if(key == null || this.number <= 0){
// System.out.println("non-leaf node, insert key: " + node1.keys[node1.number - 1] + " " + node2.keys[node2.number - 1] + "direct insert");
                this.keys[0] = node1.keys[node1.number - 1];
                this.keys[1] = node2.keys[node2.number - 1];
                this.childs[0] = node1;
                this.childs[1] = node2;
                this.number += 2;
                return this;
            }
            
            int i = 0;
            while(key.compareTo((V)this.keys[i]) != 0){
                i++;
            }
            //The maximum value of the left node can be inserted directly, and the right side should be moved and inserted.
            this.keys[i] = node1.keys[node1.number - 1];
            this.childs[i] = node1;

            Object tempKeys[] = new Object[maxNumber];
            Object tempChilds[] = new Node[maxNumber];

            System.arraycopy(this.keys, 0, tempKeys, 0, i + 1);
            System.arraycopy(this.childs, 0, tempChilds, 0, i + 1);
            System.arraycopy(this.keys, i + 1, tempKeys, 0, this.number - i - 1);
            System.arraycopy(this.childs, i + 1, tempChilds, 0, this.number - i - 1);
            tempKeys[i + 1] = node2.keys[node2.number - 1];
            tempChilds[i + 1] = node2;

            this.number++;

            
            if(this.number <= bTreeOrder){
                System.arraycopy(tempKeys, 0, this.keys, 0, this.number);
                System.arraycopy(tempChilds, 0, this.childs, 0, this.number);

// System.out.println("non-leaf node, insert key: " + node1.keys[node1.number - 1] + " " + node2.keys[node2.number - 1] + ", no split required" );

                return null;
            }

// System.out.println("non-leaf node, insert key: " + node1.keys[node1.number - 1] + " " + node2.keys[node2.number - 1] + ", need to split") ;

            Integer middle = this.number / 2;

            BPlusNode<T, V> tempNode = new BPlusNode<T, V>();
            
            tempNode.number = this.number - middle;
            tempNode.parent = this.parent;
            //If the parent node is empty, create a new non-leaf node as the parent node, and let the pointers of the two non-leaf nodes that are successfully split point to the parent node.
            if(this.parent == null) {

// System.out.println("non-leaf node, insert key: " + node1.keys[node1.number - 1] + " " + node2.keys[node2.number - 1] + ", create new parent node") ;

                BPlusNode<T, V> tempBPlusNode = new BPlusNode<>();
                tempNode.parent = tempBPlusNode;
                this.parent = tempBPlusNode;
                oldKey = null;
            }
            System.arraycopy(tempKeys, middle, tempNode.keys, 0, tempNode.number);
            System.arraycopy(tempChilds, middle, tempNode.childs, 0, tempNode.number);
            for(int j = 0; j < tempNode.number; j++){
                tempNode.childs[j].parent = tempNode;
            }
            
            this.number = middle;
            this.keys = new Object[maxNumber];
            this.childs = new Node[maxNumber];
            System.arraycopy(tempKeys, 0, this.keys, 0, middle);
            System.arraycopy(tempChilds, 0, this.childs, 0, middle);

            //After the leaf node is successfully split, the newly generated node needs to be inserted into the parent node.
            BPlusNode<T, V> parentNode = (BPlusNode<T, V>)this.parent;
            return parentNode.insertNode(this, tempNode, oldKey);
        }

    }

    /**
           * Leaf node class
     * @param <T>
     * @param <V>
     */
    class LeafNode <T, V extends Comparable<V>> extends Node<T, V> {

        protected Object values[];
        protected LeafNode left;
        protected LeafNode right;

        public LeafNode(){
            super();
            this.values = new Object[maxNumber];
            this.left = null;
            this.right = null;
        }

        /**
                   * Find, classic binary search, no more comments
         * @param key
         * @return
         */
        @Override
        T find(V key) {
            if(this.number <=0)
                return null;

// System.out.println("leaf node lookup");

            Integer left = 0;
            Integer right = this.number;

            Integer middle = (left + right) / 2;

            while(left < right){
                V middleKey = (V) this.keys[middle];
                if(key.compareTo(middleKey) == 0)
                    return (T) this.values[middle];
                else if(key.compareTo(middleKey) < 0)
                    right = middle;
                else
                    left = middle;
                middle = (left + right) / 2;
            }
            return null;
        }

        /**
         *
         * @param value
         * @param key
         */
        @Override
        Node<T, V> insert(T value, V key) {

// System.out.println("leaf node, insert key: " + key);

            
            V oldKey = null;
            if(this.number > 0)
                oldKey = (V) this.keys[this.number - 1];
            
            int i = 0;
            while(i < this.number){
                if(key.compareTo((V) this.keys[i]) < 0)
                    break;
                    i++;
            }

            Object tempKeys[] = new Object[maxNumber];
            Object tempValues[] = new Object[maxNumber];
            System.arraycopy(this.keys, 0, tempKeys, 0, i);
            System.arraycopy(this.values, 0, tempValues, 0, i);
            System.arraycopy(this.keys, i, tempKeys, i + 1, this.number - i);
            System.arraycopy(this.values, i, tempValues, i + 1, this.number - i);
            tempKeys[i] = key;
            tempValues[i] = value;

            this.number++;

// System.out.println("Insert completed, current node key is:");
//            for(int j = 0; j < this.number; j++)
//                System.out.print(tempKeys[j] + " ");
//            System.out.println();

            if(this.number <= bTreeOrder){
                System.arraycopy(tempKeys, 0, this.keys, 0, this.number);
                System.arraycopy(tempValues, 0, this.values, 0, this.number);

                //It is possible that although there is no node split, the value actually inserted is greater than the original maximum, so the boundary values ​​of all parent nodes are updated.
                Node node = this;
                while (node.parent != null){
                    V tempkey = (V)node.keys[node.number - 1];
                    if(tempkey.compareTo((V)node.parent.keys[node.parent.number - 1]) > 0){
                        node.parent.keys[node.parent.number - 1] = tempkey;
                        node = node.parent;
                    }
                }
// System.out.println("leaf node, insert key: " + key + ", no splitting is required);

                return null;
            }

// System.out.println("leaf node, insert key: " + key + ", need to split");

            Integer middle = this.number / 2;

            LeafNode<T, V> tempNode = new LeafNode<T, V>();
            tempNode.number = this.number - middle;
            tempNode.parent = this.parent;
            //If the parent node is empty, create a new non-leaf node as the parent node, and let the pointers of the two leaf nodes that are successfully split point to the parent node.
            if(this.parent == null) {

// System.out.println("leaf node, insert key: " + key + ", parent node is empty, create new parent node");

                BPlusNode<T, V> tempBPlusNode = new BPlusNode<>();
                tempNode.parent = tempBPlusNode;
                this.parent = tempBPlusNode;
                oldKey = null;
            }
            System.arraycopy(tempKeys, middle, tempNode.keys, 0, tempNode.number);
            System.arraycopy(tempValues, middle, tempNode.values, 0, tempNode.number);

            this.number = middle;
            this.keys = new Object[maxNumber];
            this.values = new Object[maxNumber];
            System.arraycopy(tempKeys, 0, this.keys, 0, middle);
            System.arraycopy(tempValues, 0, this.values, 0, middle);

            this.right = tempNode;
            tempNode.left = this;

            //After the leaf node is successfully split, the newly generated node needs to be inserted into the parent node.
            BPlusNode<T, V> parentNode = (BPlusNode<T, V>)this.parent;
            return parentNode.insertNode(this, tempNode, oldKey);
        }

        @Override
        LeafNode<T, V> refreshLeft() {
            if(this.number <= 0)
                return null;
            return this;
        }
    }
    
    public void parse(byte[] content, boolean show_log, int key){
        
        if(content == null) {
            System.out.println("Not Found Content For Page " + key);
            return;
        }
        
        int numBytesInSdtnameField = constants.STD_NAME_SIZE;
        int numBytesIntField = Integer.BYTES;
        byte[] sdtnameBytes = new byte[numBytesInSdtnameField];
        byte[] idBytes = new byte[constants.ID_SIZE];
        byte[] dateBytes = new byte[constants.DATE_SIZE];
        byte[] yearBytes = new byte[constants.YEAR_SIZE];
        byte[] monthBytes = new byte[constants.MONTH_SIZE];
        byte[] mdateBytes = new byte[constants.MDATE_SIZE];
        byte[] dayBytes = new byte[constants.DAY_SIZE];
        byte[] timeBytes = new byte[constants.TIME_SIZE];
        byte[] sensorIdBytes = new byte[constants.SENSORID_SIZE];
        byte[] sensorNameBytes = new byte[constants.SENSORNAME_SIZE];
        byte[] countsBytes = new byte[constants.COUNTS_SIZE];
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

        System.arraycopy(content, 0, sdtnameBytes, 0, numBytesInSdtnameField);

        String sdtNameString = new String(sdtnameBytes);

        System.arraycopy(content, constants.ID_OFFSET, idBytes, 0, numBytesIntField);
        System.arraycopy(content, constants.DATE_OFFSET, dateBytes, 0, constants.DATE_SIZE);
        System.arraycopy(content, constants.YEAR_OFFSET, yearBytes, 0, numBytesIntField);
        System.arraycopy(content, constants.MONTH_OFFSET, monthBytes, 0, constants.MONTH_SIZE);
        System.arraycopy(content, constants.MDATE_OFFSET, mdateBytes, 0, numBytesIntField);
        System.arraycopy(content, constants.DAY_OFFSET, dayBytes, 0, constants.DAY_SIZE);
        System.arraycopy(content, constants.TIME_OFFSET, timeBytes, 0, numBytesIntField);
        System.arraycopy(content, constants.SENSORID_OFFSET, sensorIdBytes, 0, numBytesIntField);
        System.arraycopy(content, constants.SENSORNAME_OFFSET, sensorNameBytes, 0, constants.SENSORNAME_SIZE);
        System.arraycopy(content, constants.COUNTS_OFFSET, countsBytes, 0, numBytesIntField);

        // Convert long data into Date object
        Date date = new Date(ByteBuffer.wrap(dateBytes).getLong());

        // Get a string representation of the record for printing to stdout
        String record = sdtNameString.trim() + "," + ByteBuffer.wrap(idBytes).getInt()
                                   + "," + dateFormat.format(date) + "," + ByteBuffer.wrap(yearBytes).getInt() +
                                   "," + new String(monthBytes).trim() + "," + ByteBuffer.wrap(mdateBytes).getInt()
                                   + "," + new String(dayBytes).trim() + "," + ByteBuffer.wrap(timeBytes).getInt()
                                   + "," + ByteBuffer.wrap(sensorIdBytes).getInt() + "," +
                                   new String(sensorNameBytes).trim() + "," + ByteBuffer.wrap(countsBytes).getInt();
        if(show_log)
           System.out.println(record);
    }
    
    public static void main(String[] args) throws IOException
    {
        // check for correct number of arguments
        if (args.length != constants.DBQUERY_ARG_COUNT) {
            System.out.println("Error: Incorrect number of arguments were input");
            return;
        }

        int esv1 = Integer.parseInt(args[constants.EQUERY_ARG1]);
        int esv2 = Integer.parseInt(args[constants.EQUERY_ARG2]);
        int rsv1 = Integer.parseInt(args[constants.RQUERY_ARG1]);
        int rsv2 = Integer.parseInt(args[constants.RQUERY_ARG2]);
        int esv3 = Integer.parseInt(args[constants.EQUERY_ARG3]);
        int esv4 = Integer.parseInt(args[constants.EQUERY_ARG4]);
        int rsv3 = Integer.parseInt(args[constants.RQUERY_ARG3]);
        int rsv4 = Integer.parseInt(args[constants.RQUERY_ARG4]);
        int fanv1 = Integer.parseInt(args[constants.FAN_ARG1]);
        int fanv2 = Integer.parseInt(args[constants.FAN_ARG2]);
        
        long setTime = 0;
        long endTime = 0;
        
        setTime = System.nanoTime();
        
        BTree bt = new BTree(fanv1);
        
        int rid = 0;
        int rSize = Integer.parseInt(args[constants.DBQUERY_PAGE_SIZE_ARG]);

        String file = "heap." + rSize;
        int bytesOneRecord = constants.TOTAL_SIZE;
        int bytesSdtnameField = constants.STD_NAME_SIZE;
        int recordsPerPage = rSize/bytesOneRecord;
        byte[] content = new byte[rSize];
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(file);
            int readBytes = 0;
            byte[] sdtnameBytes = new byte[bytesSdtnameField];
            
            while ((readBytes = inputStream.read(content)) != -1) {
                    for (int i = 0; i < recordsPerPage; i++) {
                    byte[] recordBytes = new byte[constants.TOTAL_SIZE];
                    System.arraycopy(content, (i*bytesOneRecord), sdtnameBytes, 0, bytesSdtnameField);

                    if (sdtnameBytes[0] == 0) {
                        break;
                    }

                    System.arraycopy(content, (i*bytesOneRecord), recordBytes, 0, constants.TOTAL_SIZE);
                    
                    rid++;
                    bt.insert(recordBytes, rid);
                }
            }
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        finally {

            if (inputStream != null) {
                inputStream.close();
            }
        }

        for(int i=rsv1;i<=rsv2;i++)
            bt.parse((byte[])bt.find(i), false, i);
        //bt.find(rsv1, rsv2, false);
        setTime = System.nanoTime();
        //equality search
        System.out.println("equality search with Fanout " + fanv1);
        bt.parse((byte[])bt.find(esv1), true, esv1);
        bt.parse((byte[])bt.find(esv2), true, esv2);
        
        //range search
        System.out.println("range search with Fanout " + fanv1);
        for(int i=rsv1;i<=rsv2;i++)
            bt.parse((byte[])bt.find(i), true, i);
        
        endTime = System.nanoTime();
        
        double time = (double)(endTime - setTime)/(double)(constants.MILLISECONDS_PER_SECOND);
        System.out.println("Took " + time + " ms");
        
        System.out.println();
        
        setTime = System.nanoTime();
        
        bt = new BTree(fanv2);
        
        rid = 0;
        rSize = Integer.parseInt(args[constants.DBQUERY_PAGE_SIZE_ARG]);

        file = "heap." + rSize;
        bytesOneRecord = constants.TOTAL_SIZE;
        bytesSdtnameField = constants.STD_NAME_SIZE;
        recordsPerPage = rSize/bytesOneRecord;
        content = new byte[rSize];
        
        try {
            inputStream = new FileInputStream(file);
            int readBytes = 0;
            byte[] sdtnameBytes = new byte[bytesSdtnameField];
            
            while ((readBytes = inputStream.read(content)) != -1) {
                    for (int i = 0; i < recordsPerPage; i++) {
                    byte[] recordBytes = new byte[constants.TOTAL_SIZE];
                    System.arraycopy(content, (i*bytesOneRecord), sdtnameBytes, 0, bytesSdtnameField);

                    if (sdtnameBytes[0] == 0) {
                        break;
                    }

                    System.arraycopy(content, (i*bytesOneRecord), recordBytes, 0, constants.TOTAL_SIZE);
                    
                    rid++;
                    bt.insert(recordBytes, rid);
                }
            }
        }
        catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        finally {

            if (inputStream != null) {
                inputStream.close();
            }
        }

        for(int i=rsv3;i<=rsv4;i++)
            bt.parse((byte[])bt.find(i), false, i);
        //bt.find(rsv1, rsv2, false);
        setTime = System.nanoTime();
        //equality search
        System.out.println("equality search with Fanout " + fanv2);
        bt.parse((byte[])bt.find(esv3), true, esv3);
        bt.parse((byte[])bt.find(esv4), true, esv4);
        
        //range search
        System.out.println("range search with Fanout " + fanv2);
        for(int i=rsv3;i<=rsv4;i++)
            bt.parse((byte[])bt.find(i), true, i);
        
        endTime = System.nanoTime();
        
        time = (double)(endTime - setTime)/(double)(constants.MILLISECONDS_PER_SECOND);
        System.out.println("Took " + time + " ms");
        
        System.out.println();
    }
}
