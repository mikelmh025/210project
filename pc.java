import java.security.SecureRandom; 
import java.util.Arrays; 
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.HashSet;
import java.util.Set;

public class pc
{
    class consumer implements Runnable
    {
        private int num_ops;
        private final int n_consumers;
        private final int tid;
		public consumer(int num_ops, int n_consumers,int tid){
            this.num_ops = num_ops;
            this.n_consumers = n_consumers;
			this.tid = tid;
        }
        
        public void run(){
            try{
                System.out.println("start comsumer" + tid + " queue " +  values.qm[tid].size());
                for (int i = 0; i < values.NUM_ITER/n_consumers; i++) 
                    num_ops += consume_multiq(tid);

            } catch (Exception e){
                e.printStackTrace();
            }
        }

        int consume_multiq(int qid){
            try{ 

                

                int num_ops = 0;

                values.qm_mtx[qid].lock();
                while (values.qm[qid].isEmpty()) {
                    values.qm_mtx[qid].unlock();
                    values.qm_mtx[qid].lock();
                }

                task t = values.qm[qid].take();
                values.qm_mtx[qid].unlock();

                // Start to process the set
                values.s_mtx.lock();
                // System.out.println("test" + values.s.add(t.item));
                
                if(t.action == 0){
                    Boolean temp = values.s.add(t.item);
                    if (temp) {
                        num_ops += 1;
                    }
                } else if (t.action == 1){
                    Boolean temp = values.s.remove(t.item);
                    if (temp) {
                        num_ops += 1;
                    }
                } else{
                    Boolean temp = values.s.contains(t.item);
                    if (temp) {
                        num_ops += 1;
                    }
                }
                values.s_mtx.unlock();
                return num_ops;

            } catch (Exception e){
                e.printStackTrace();
            }
            return num_ops;
        }
    }

    class producer implements Runnable
    {
        private final long seed;
        private final int n_consumers;
		public producer(long seed, int n_consumers){
            this.seed = seed;
            this.n_consumers = n_consumers;
        }
        
        public void run(){
            try{
                System.out.println("start producer" );
                for (int i = 0; i < values.NUM_ITER/values.NUM_PRODUCE; i++){
                    produce_multiq_batch(i % this.n_consumers, seed);
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }

        // Producer produces multiple tasks once 
        // and append them at the corresponding queue
        void produce_multiq_batch(int qid, long seed){
            try{
                values.qm_mtx[qid].lock();
                while(values.qm[qid].size()>100){
                    // System.out.println("sleep "+ values.qm[qid].size() + " qid " + qid);
                    values.qm_mtx[qid].unlock();
                    // Producer shoud sleep for a little while
                    Thread.sleep(10);
                    values.qm_mtx[qid].lock();
                }
                // System.out.println("aha!!");
        
                for (int i = 0; i < values.NUM_PRODUCE; i++) {
                    task t = new task();
                    int action = values.rand.nextInt(values.LOOKUP_RATIO);

                    if (action == 0){
                        t.action = 0;
                    }else if (action == 1){
                        t.action = 1;
                    }else{
                        t.action = 2; 
                    }
                    t.item = values.rand.nextInt(values.NUM_ITEM);
                    values.qm[qid].put(t);
                    // System.out.println("after put t.item: "+ t.item);   
                }
                values.qm_mtx[qid].unlock();
            }catch (Exception e){
                e.printStackTrace();
            }
            
        }
    }

    

    private void test(){
        try{
            for(int i=0; i<values.qm.length; i++){
                values.qm[i]=new LinkedBlockingQueue(); //change constructor as needed
            }
            for(int i=0; i<values.MAX_THREADS; i++){
                values.qm_mtx[i] = new ReentrantLock();
                values.sm[i] = new HashSet<Integer>();
            }

            // Make it takes args
            int max_thread = 33;

            for (int num_thread = 1; num_thread < max_thread; num_thread *= 2){

                int[] num_ops = new int[num_thread];
                int total_num_ops = 0;
                Thread[] consumers = new Thread[num_thread];

                long seed = 0;
                // init_partitioned(num_threads); //TODO: what is this?

                long start = System.currentTimeMillis();

                Thread producer = new Thread(new producer(seed, num_thread)); //TODO: Finish this
                producer.start();
                for (int tid = 0; tid < num_thread; tid++) {
					consumers[tid] = new Thread(new consumer(num_ops[tid], num_thread, tid)); //TODO : change this
					consumers[tid].start();
                }
                producer.join();
				for (int tid = 0; tid < num_thread; tid++) {
                    consumers[tid].join(); //todo add catch exception
                    total_num_ops += num_ops[tid];
                }
                
				long end = System.currentTimeMillis();

				long elapsed = end - start;
				System.out.println(elapsed);
			}

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    class values{
        public final int LOOKUP_RATIO = 10;
        public final int NUM_ITEM = 1000000;
        public final int NUM_ITER = 1024*1024;
        public final int MAX_THREADS = 64;
        public final int NUM_PRODUCE = 128;
        public final int NUM_SETS = 32;

        public Lock q_mtx = new ReentrantLock();// A global lock on one task queue
        public BlockingQueue<task> q;

        public Lock qm_mtx[] = new ReentrantLock[MAX_THREADS];
        public BlockingQueue<task>[] qm = new LinkedBlockingQueue[MAX_THREADS];


        public Lock s_mtx = new ReentrantLock();// A global lock on one element set
        public Set<Integer> s = new HashSet<Integer>();

        public Set<Integer>[] sm = new HashSet[MAX_THREADS];
        // private final vector<Lock> sm_mtx[NUM_SETS];

        public SecureRandom rand = new SecureRandom();


    }
    final values values = new values();

    class task{
        public int action;
        public int item;
    }

    public static void main(String[] args)
    {
        try{
            pc test = new pc();
            test.test();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}