/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor
{
	// (Task 2): Define possible states of a philosopher
	private enum State {THINKING, HUNGRY, EATING, TALKING}

	// (Task 2): Array to store the state of each philosopher
	private State[] state;

	// (Task 2): Total number of philosophers
	private int numOfPhilosophers;

	// (Task 2): Time duration for which a philosopher talks
	private static final int TALKING_TIME = 1000; // for example, 1 second to talk

	// (Task 2): Additional field to track the status of each chopstick
	private boolean[] chopstickAvailable;

	// (Task 3): Array to store the priority of each philosopher
	private int[] priorities;

	// (Task 6): Add two boolean variables to represent the two pepper shakers
	private boolean pepperShaker1 = true;
	private boolean pepperShaker2 = true;

	// (Task 6): Add an array to remember which pepper shaker each philosopher has
	private int[] philosopherPepperShaker;


	/**
	 * Constructor
	 */
	// (Task 2): Constructor to initialize the state of each philosopher as THINKING
	public Monitor(int piNumberOfPhilosophers) {
		numOfPhilosophers = piNumberOfPhilosophers;
		state = new State[numOfPhilosophers];
		priorities = new int[numOfPhilosophers]; // (Task 3): Initialize the priority array

		// (Task 6): Initialize the philosopherPepperShaker array before using it
		philosopherPepperShaker = new int[numOfPhilosophers];
		chopstickAvailable = new boolean[numOfPhilosophers];

		for (int i = 0; i < numOfPhilosophers; i++) {
			state[i] = State.THINKING;
			priorities[i] = i + 1;  // (Task 3): Initialize the priority, 1 is the highest and increases by 1
			philosopherPepperShaker[i] = 0;  // (Task 6): 0 means no pepper shaker, 1 means pepper shaker 1, and 2 means pepper shaker 2
			chopstickAvailable[i] = true;  // Initialize all chopsticks as available
		}
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	// (Task 2): Check if a philosopher (identified by piTID) can pick up the chopsticks to eat
	private synchronized boolean canPickup(int piTID) {
		int left = (piTID - 1) % numOfPhilosophers;  // piTID starts at 1, so subtract 1 to get the correct index
		int right = piTID % numOfPhilosophers;

		// (Task 3): Check if a philosopher with a higher priority is waiting to eat
		for (int i = 0; i < numOfPhilosophers; i++) {
			if (state[i] == State.HUNGRY && priorities[i] < priorities[piTID - 1]) {
				return false;  // A philosopher with a higher priority is waiting
			}
		}

		// A philosopher can pick up chopsticks if they are HUNGRY and neither of their neighbors are EATING, and chopsticks are available
		return state[piTID-1] == State.HUNGRY
				&& chopstickAvailable[left]
				&& chopstickAvailable[right]
				&& state[left] != State.EATING
				&& state[right] != State.EATING;
	}

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	// (Task 2): Allow a philosopher to pick up the forks if they can, or make them wait
	public synchronized void pickUp(final int piTID) {
		state[piTID-1] = State.HUNGRY;  // Mark the philosopher as HUNGRY
		while (!canPickup(piTID)) {   // Wait until the philosopher can pick up the forks
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		// Mark the philosopher's chopsticks as unavailable
		int left = (piTID - 1) % numOfPhilosophers;
		int right = piTID % numOfPhilosophers;
		chopstickAvailable[left] = false;
		chopstickAvailable[right] = false;

		// (Task 6): Acquire a pepper shaker before starting to eat
		acquirePepperShaker(piTID);
		state[piTID-1] = State.EATING;  // Mark the philosopher as EATING
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	// (Task 2): Allow a philosopher to put down the chopsticks and notify all waiting philosophers
	public synchronized void putDown(final int piTID) {
		// (Task 6): Release the pepper shaker after finishing eating
		releasePepperShaker(piTID);
		state[piTID-1] = State.THINKING;  // Mark the philosopher as THINKING

		// Mark the philosopher's chopsticks as available
		int left = (piTID - 1) % numOfPhilosophers;
		int right = piTID % numOfPhilosophers;
		chopstickAvailable[left] = true;
		chopstickAvailable[right] = true;


		notifyAll();  // Notify all waiting philosophers
	}

	// (Task 2): Check if any philosopher is currently talking
	private synchronized boolean canTalk() {
		for (int i = 0; i < numOfPhilosophers; i++) {
			if (state[i] == State.TALKING) {  // If a philosopher is found talking, return false
				return false;
			}
		}
		return true;  // No philosopher is talking, so return true
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	// (Task 2): Allow a philosopher to talk if no one else is, or make them wait
	public synchronized void requestTalk(final int piTID) {
		while (!canTalk()) {
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		state[piTID-1] = State.TALKING;  // Directly set the state of requesting philosopher to TALKING
		try {
			wait(TALKING_TIME);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	// (Task 2): Allow a philosopher to end their talk and notify all waiting philosophers
	public synchronized void endTalk(final int piTID) {
		state[piTID-1] = State.THINKING;  // Directly set the state of philosopher who finished talking to THINKING
		notifyAll();
	}

	// (Task 6): Method to check if any pepper shaker is available
	private synchronized boolean isPepperShakerAvailable() {
		return pepperShaker1 || pepperShaker2;
	}

	// (Task 6): Method for a philosopher to acquire a pepper shaker
	private synchronized void acquirePepperShaker(int philosopherTID) {
		while (!isPepperShakerAvailable()) {
			try {
				System.out.println("Philosopher " + philosopherTID + " is waiting for a pepper shaker");
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		if (pepperShaker1) {
			pepperShaker1 = false;
			philosopherPepperShaker[philosopherTID - 1] = 1;
			System.out.println("Philosopher " + philosopherTID + " has acquired pepper shaker 1");
		} else {
			pepperShaker2 = false;
			philosopherPepperShaker[philosopherTID - 1] = 2;
			System.out.println("Philosopher " + philosopherTID + " has acquired pepper shaker 2");
		}
	}

	// (Task 6): Method for a philosopher to release a pepper shaker
	private synchronized void releasePepperShaker(int philosopherTID) {
		if (philosopherPepperShaker[philosopherTID - 1] == 1) {
			pepperShaker1 = true;
			System.out.println("Philosopher " + philosopherTID + " has released pepper shaker 1");
		} else if (philosopherPepperShaker[philosopherTID - 1] == 2) {
			pepperShaker2 = true;
			System.out.println("Philosopher " + philosopherTID + " has released pepper shaker 2");
		}
		philosopherPepperShaker[philosopherTID - 1] = 0;  // reset the philosopher's pepper shaker
		notifyAll();
	}
}

// EOF
