/**
 * Class DiningPhilosophers
 * The main starter.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class DiningPhilosophers
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */

	/**
	 * This default may be overridden from the command line
	 */
	public static final int DEFAULT_NUMBER_OF_PHILOSOPHERS = 4;

	/**
	 * Dining "iterations" per philosopher thread
	 * while they are socializing there
	 */
	public static final int DINING_STEPS = 10;

	/**
	 * Our shared monitor for the philosphers to consult
	 */
	public static Monitor soMonitor = null;

	/*
	 * -------
	 * Methods
	 * -------
	 */

	/**
	 * Main system starts up right here
	 */
	public static void main(String[] argv)
	{
		try
		{
			/*
			 * TODO:
			 * Should be settable from the command line
			 * or the default if no arguments supplied.
			 */
			int iPhilosophers = DEFAULT_NUMBER_OF_PHILOSOPHERS;

			// (Task 4): Check if the user provided command-line arguments. If none are provided we keep the default number of philosophers (declared above).
			// For example, from terminal we can compile with javac DiningPhilosophers.java, then run java DiningPhilosophers 7 to set 7 philosophers.
			if(argv.length == 1) {
				try {
					// If one argument was provided, we try to parse the number of philosophers from the given argument.
					iPhilosophers = Integer.parseInt(argv[0]);

					// We ensure that the provided number is positive, and throw an error if it isn't.
					if(iPhilosophers <= 0) {
						System.out.println("\"" + argv[0] + "\" is not a positive decimal intege");
						System.out.println("Usage: java DiningPhilosophers [NUMBER_OF_PHILOSOPHERS]");
						System.exit(1);

					}

				} catch(NumberFormatException e) {
					// Error handling for numbers provided that are negative or zero:
					System.out.println("\"" + argv[0] + "\" is not a positive decimal integer");
					System.out.println("Usage: java DiningPhilosophers [NUMBER_OF_PHILOSOPHERS]");
					System.exit(1);
				}
			} else if(argv.length > 1) {
				// If more than one argument is passed from the command line, we exit the program
				System.out.println("Too many arguments (1 expected, " + argv.length +" provided)! Please provide only the number of philosophers (must be a positive, non-zero integer).");
				System.out.println("Usage: java DiningPhilosophers [NUMBER_OF_PHILOSOPHERS]");
				System.exit(1);
			}

			// Make the monitor aware of how many philosophers there are
			soMonitor = new Monitor(iPhilosophers);

			// Space for all the philosophers
			Philosopher aoPhilosophers[] = new Philosopher[iPhilosophers];

			// Let 'em sit down
			for(int j = 0; j < iPhilosophers; j++)
			{
				aoPhilosophers[j] = new Philosopher();
				aoPhilosophers[j].start();
			}

			System.out.println
			(
				iPhilosophers +
				" philosopher(s) came in for a dinner."
			);

			// Main waits for all its children to die...
			// I mean, philosophers to finish their dinner.
			for(int j = 0; j < iPhilosophers; j++)
				aoPhilosophers[j].join();

			System.out.println("All philosophers have left. System terminates normally.");
		}
		catch(InterruptedException e)
		{
			System.err.println("main():");
			reportException(e);
			System.exit(1);
		}
	} // main()

	/**
	 * Outputs exception information to STDERR
	 * @param poException Exception object to dump to STDERR
	 */
	public static void reportException(Exception poException)
	{
		System.err.println("Caught exception : " + poException.getClass().getName());
		System.err.println("Message          : " + poException.getMessage());
		System.err.println("Stack Trace      : ");
		poException.printStackTrace(System.err);
	}
}

// EOF
