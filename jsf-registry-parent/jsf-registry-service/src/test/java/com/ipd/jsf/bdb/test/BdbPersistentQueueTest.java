/**
 * Copyright 2004-2048 .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.bdb.test;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

public class BdbPersistentQueueTest extends TestCase {
    Queue<String> memoryQueue;
    Queue<String> persistentQueue;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        memoryQueue=new LinkedBlockingQueue<String>();
        String dbDir="J:/java/test/bdbDir";
        File file=new File(dbDir);
        if(!file.exists()||!file.isDirectory()){
            file.mkdirs();
        }
        persistentQueue = new BdbPersistentQueue(dbDir, "pq", String.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        memoryQueue.clear();
        memoryQueue=null;
        persistentQueue.clear();
        persistentQueue=null;
    }
    
    /**
     * 排放值
     * @param queue
     * @return      排放的数据个数
     */
    public int drain(Queue<String> queue){
        int count=0;
        while(true){
            try {
                queue.remove();
                count++;
            } catch (Exception e) {
                return count;
            }
        }
       
    }
    /**
     * 
     * @param queue
     * @param size
     */
    public void fill(Queue<String> queue,int size){
        for(int i=0;i<size;i++){
            queue.add(i+"");
        }
    }
    
    public void checkTime(int size){
        System.out.println("1.内存Queue插入和排空数据所耗时间");
        long time=0;
        long start=System.nanoTime();
        fill(memoryQueue,size);
        time=System.nanoTime()-start;
        System.out.println("\t填充 "+size+" 条数据耗时: "+(double)time/1000000+" 毫秒,单条耗时: "+((double)time/size)+" 纳秒");
        start=System.nanoTime();
        drain(memoryQueue);
        time=System.nanoTime()-start;
        System.out.println("\t排空 "+size+" 条数据耗时: "+(double)time/1000000+" 毫秒,单条耗时: "+((double)time/size)+" 纳秒");
        
        System.out.println("2.持久化Queue插入和排空数据所耗时间");
        start=System.nanoTime();
        fill(persistentQueue,size);
        time=System.nanoTime()-start;
        System.out.println("\t填充 "+size+" 条数据耗时: "+(double)time/1000000+" 毫秒,单条耗时: "+((double)time/size/1000000)+" 豪秒");
        start=System.nanoTime();
        drain(persistentQueue);
        time=System.nanoTime()-start;
        System.out.println("\t排空 "+size+" 条数据耗时: "+(double)time/1000000+" 毫秒,单条耗时: "+((double)time/size/1000)+" 豪秒");
        
    }
    
    /**
     * 十万数量级测试
     */
    public void testTime_tenThousand(){
        System.out.println("========测试1000000(十万)条数据=================");
        checkTime(100000);
    }
    
    
    /**
     * 百万数量级测试
     */
    public void testTime_mil(){
        System.out.println("========测试1000000(百万)条数据=================");
        checkTime(1000000);
    }
    

    /**
     * 千万数量级测试,注意要防止内存溢出
     */
    public void testTime_tenMil(){
//        System.out.println("========测试10000000(千万)条数据=================");
//        checkTime(10000000);
    }
    
    /**
     * 测试队列数据准确性
     * @param queue
     * @param queueName
     * @param size
     */
    public void checkDataExact(Queue<String> queue,String queueName,int size){
        if(queue.size()!=size){
            System.err.println("Error size of "+queueName);
        }
        String value=null;
        for(int i=0;i<size;i++){
            value=queue.remove();
            if(!((i+"").equals(value))){
                System.err.println("Error "+queueName+":"+i+"->"+value);
            }
        }
    }
    
    /**
     * 测试队列中数据的准确性,包括长度
     */
    public void testExact(){
        int size=100;
        fill(memoryQueue,size);
        fill(persistentQueue,size);
        
        checkDataExact(memoryQueue,"MemoryQueue",100);
        checkDataExact(persistentQueue,"PersistentQueue",100);
         
    }
    
}
