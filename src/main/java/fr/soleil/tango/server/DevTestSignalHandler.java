package fr.soleil.tango.server;

import java.util.HashMap;
import java.util.Map;

import org.tango.utils.DevFailedUtils;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import fr.esrf.Tango.DevFailed;

public class DevTestSignalHandler implements SignalHandler {

    // Signal Name Description Disabled by -Xrs z/OS AIX Windows
    // SIGSEGV Incorrect access to memory (write to inaccessible memory) No Yes Yes Yes
    // SIGILL Illegal instruction (attempt to invoke a unknown machine instruction) No Yes Yes Yes
    // SIGFPE Floating point exception (divide by zero) No Yes Yes Yes
    // SIGBUS Bus error (attempt to address nonexistent memory location) Yes Yes Yes No
    // SIGSYS Bad system call issued Yes Yes Yes No
    // SIGXCPU CPU time limit exceeded (you've been running too long!) Yes Yes Yes No
    // SIGXFSZ File size limit exceeded Yes Yes Yes No
    // SIGEMT EMT instruction (AIX specific) Yes No Yes No
    // SIGABRT Abnormal termination. The JVM raises this signal whenever it detects a JVM fault. Yes Yes Yes Yes
    // SIGINT Interactive attention (CTRL-C). JVM will exit normally. Yes Yes Yes Yes
    // SIGTERM Termination request. JVM will exit normally. Yes Yes Yes Yes
    // SIGHUP Hang up. JVM will exit normally. Yes Yes Yes No
    // SIGUSR1 User defined. Used by some JVMs for internal control purposes. No Yes No No
    // SIGUSR2 User defined. Used by some JVMs for internal control purposes. No No Yes No
    // SIGQUIT A quit signal for a terminal. JVM uses this for taking Java core dumps. Yes Yes Yes No
    // SIGBREAK A break signal from a terminal. JVM uses this for taking Java core dumps. Yes No No Yes
    // SIGTRAP Internal for use by dbx or ptrace. Used by some JVMs for internal control purposes. Yes (not for AIX) Yes
    // Yes No
    // SIGPIPE A write to a pipe that is not being read. JVM ignores this.

    // 1 SIGHUP Instruction (HANG UP) - Fin de session
    // 2 SIGINT Interruption
    // 3 SIGQUIT Instruction (QUIT)
    // 4 SIGILL Instruction illégale
    // 5 SIGTRAP Trace trap
    // 6 SIGABRT (ANSI) Instruction (ABORT)
    // 6 SIGIOT (BSD) IOT Trap
    // 7 SIGBUS Bus error
    // 8 SIGFPE Floating-point exception - Exception arithmétique
    // 9 SIGKILL Instruction (KILL) - termine le processus immédiatement
    // 10 SIGUSR1 Signal utilisateur 1
    // 11 SIGSEGV Violation de mémoire
    // 12 SIGUSR2 Signal utilisateur 2
    // 13 SIGPIPE Broken PIPE - Erreur PIPE sans lecteur
    // 14 SIGALRM Alarme horloge
    // 15 SIGTERM Signal de terminaison
    // 16 SIGSTKFLT Stack Fault
    // 17 SIGCHLD ou SIGCLD modification du statut d'un processus fils
    // 18 SIGCONT Demande de reprise du processus
    // 19 SIGSTOP Demande de suspension imbloquable
    // 20 SIGTSTP Demande de suspension depuis le clavier
    // 21 SIGTTIN lecture terminal en arrière-plan
    // 22 SIGTTOU écriture terminal en arrière-plan
    // 23 SIGURG évènement urgent sur socket
    // 24 SIGXCPU temps maximum CPU écoulé
    // 25 SIGXFSZ taille maximale de fichier atteinte
    // 26 SIGVTALRM alarme horloge virtuelle
    // 27 SIGPROF Profiling alarm clock
    // 28 SIGWINCH changement de taille de fenêtre
    // 29 SIGPOLL (System V) occurence d'un évènement attendu
    // 29 SIGIO (BSD) I/O possible actuellement
    // 30 SIGPWR Power failure restart
    // 31 SIGSYS Erreur d'appel système
    // 31 SIGUNUSED
    private final static Map<Integer, String> signalNumberName = new HashMap<Integer, String>();
    static {
	signalNumberName.put(1, "HUP");
	signalNumberName.put(2, "INT");
	signalNumberName.put(3, "QUIT");
	signalNumberName.put(4, "ILL");
	signalNumberName.put(5, "TRAP");
	signalNumberName.put(6, "ABRT");
	signalNumberName.put(7, "BUS");
	signalNumberName.put(8, "FPE");
	signalNumberName.put(9, "KILL");
	signalNumberName.put(10, "USR1");
	signalNumberName.put(14, "ALRM");
	signalNumberName.put(15, "TERM");
	signalNumberName.put(16, "STKFLT");
	signalNumberName.put(17, "HLD");
	signalNumberName.put(18, "CONT");
	signalNumberName.put(19, "STOP");
	signalNumberName.put(20, "STP");
	signalNumberName.put(21, "TIN");
	signalNumberName.put(22, "TOU");
	signalNumberName.put(23, "URG");
	signalNumberName.put(24, "XCPU");
	signalNumberName.put(25, "XFSZ");
	signalNumberName.put(26, "VTALRM");
	signalNumberName.put(27, "PROF");
	signalNumberName.put(28, "WINCH");
	signalNumberName.put(29, "POLL");
	signalNumberName.put(30, "PWR");
	signalNumberName.put(31, "SYS");
    }

    private SignalHandler oldHandler;

    public static void checkSignal(final int signalNr) throws DevFailed {
	if (signalNr < 1 || signalNr > 31) {
	    DevFailedUtils.throwDevFailed("API_SignalOutOfRange", signalNr + " is not a valid signal");
	}
    }

    // Static method to install the signal handler
    public static void install(final int signalNr) throws DevFailed {
	System.out.println("install " + signalNr);
	checkSignal(signalNr);
	final String signalName = signalNumberName.get(signalNr);
	System.out.println("install " + signalName);
	if (signalName == null) {
	    DevFailedUtils.throwDevFailed("API_SignalOutOfRange", signalNr + " is not a valid signal");
	}
	// final Signal diagSignal = new Signal(signalName);
	// final DevTestSignalHandler diagHandler = new DevTestSignalHandler();
	// diagHandler.oldHandler = Signal.handle(diagSignal, diagHandler);
	// return diagHandler;
    }

    // Static method to install the signal handler
    public static DevTestSignalHandler install(final String signalName) {
	final Signal diagSignal = new Signal(signalName);
	final DevTestSignalHandler diagHandler = new DevTestSignalHandler();
	diagHandler.oldHandler = Signal.handle(diagSignal, diagHandler);
	return diagHandler;
    }

    // Signal handler method
    public void handle(final Signal sig) {
	System.out.println("Diagnostic Signal handler called for signal " + sig);
	try {
	    // Output information for each thread
	    final Thread[] threadArray = new Thread[Thread.activeCount()];
	    final int numThreads = Thread.enumerate(threadArray);
	    System.out.println("Current threads:");
	    for (int i = 0; i < numThreads; i++) {
		System.out.println("    " + threadArray[i]);
	    }

	    // Chain back to previous handler, if one exists
	    if (oldHandler != SIG_DFL && oldHandler != SIG_IGN) {
		oldHandler.handle(sig);
	    }

	} catch (final Exception e) {
	    System.out.println("Signal handler failed, reason " + e);
	}
    }

}
