// Java code for thread creation by extending 
// the Thread class 

import java.security.SecureRandom; 
import java.util.Arrays; 
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Main Class 
public class bank 
{ 
	class MultithreadingDemo implements Runnable
	{ 
		private final int tid;
		public MultithreadingDemo(int tid){
			this.tid = tid;
		}
		public void run() 
		{ 
			try
			{ 
				long tid = Thread.currentThread().getId();

				long s0 = tid + 1, s1 = (tid + 1) * 2, s2 = (tid + 1) * 3;
				int d, s, amount;

				for (long i=0; i < account.ITER; i++){
					s = account.rand.nextInt(account.SIZE); 
					d = account.rand.nextInt(account.SIZE); 
					amount = account.rand.nextInt(account.MONEY_START_AMT); 

					// System.out.print("money[s] " + account.money[s] + ", account.money[d] "+ account.money[d]);
					// System.out.println(", s " + s + ", d " + d + ", amount "+ amount);

					if (s ==d){
						continue;
					}
					if (s>d){
						account.mutex[s].lock();
						account.mutex[d].lock();
					}
					else{
						account.mutex[d].lock();
						account.mutex[s].lock();
					}
					if((account.money[s]+account.money[d]) > 0){
						account.money[s] += amount;
						account.money[d] -= amount;
					}
					account.mutex[d].unlock();
					account.mutex[s].unlock();
				}

			} 
			catch (Exception e) 
			{ 
				// Throwing an exception 
				System.out.println ("Exception is caught"); 
			} 
		} 
	} 
	
	private void test(){ 
		try{

			Arrays.fill(account.money, account.MONEY_START_AMT); 
			for (int i = 0; i < account.SIZE; i++) {
				account.mutex[i] = new ReentrantLock();
			}

			for (int num_thread = 1; num_thread < 33; num_thread *= 2){
				long start = System.currentTimeMillis();

				Thread myThreads[] = new Thread[num_thread];
				for (int j = 0; j < num_thread; j++) {
					myThreads[j] = new Thread(new MultithreadingDemo(j));
					myThreads[j].start();
				}
				for (int j = 0; j < num_thread; j++) {
					myThreads[j].join(); //todo add catch exception
				}
				long end = System.currentTimeMillis();

				long elapsed = end - start;
				System.out.println(elapsed);
			}

			


		}catch (Exception e){
			e.printStackTrace();
		}
		
		

	}

	class account{
		
		public final long ITER = 10000000;
		public final int SIZE = 1024*1024;
		public final int MONEY_START_AMT = 1000;
		public volatile int[] money = new int[SIZE];
		private final Lock[] mutex = new ReentrantLock[SIZE];

		public SecureRandom rand = new SecureRandom();

	}
	final account account = new account();


	public static void main(String[] args) 
	{ 
		
		try {
			bank test = new bank();
			test.test();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	} 
} 


