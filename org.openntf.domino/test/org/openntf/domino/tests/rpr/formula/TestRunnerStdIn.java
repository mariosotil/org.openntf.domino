/* Generated By:JJTree&JavaCC: Do not edit this line. AtFormulaParser.java */
package org.openntf.domino.tests.rpr.formula;

import java.util.List;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.openntf.domino.impl.Base;
import org.openntf.domino.tests.rpr.formula.eval.AtFunctionFactorySystem;
import org.openntf.domino.tests.rpr.formula.eval.AtFunctionT;
import org.openntf.domino.tests.rpr.formula.eval.DefaultOperators;
import org.openntf.domino.tests.rpr.formula.eval.Formatter;
import org.openntf.domino.tests.rpr.formula.eval.FormulaContext;
import org.openntf.domino.tests.rpr.formula.parse.AtFormulaParser;
import org.openntf.domino.tests.rpr.formula.parse.SimpleNode;
import org.openntf.domino.thread.DominoThread;
import org.openntf.domino.utils.Factory;

public class TestRunnerStdIn implements Runnable {
	public static void main(final String[] args) {
		DominoThread thread = new DominoThread(new TestRunnerStdIn(), "My thread");
		thread.start();
	}

	public TestRunnerStdIn() {
		// whatever you might want to do in your constructor, but stay away from Domino objects
	}

	protected AtFormulaParser getParser() {
		Formatter formatter = new DominoFormatter();
		AtFunctionFactorySystem functionFactory = new AtFunctionFactorySystem();
		functionFactory.add(new DefaultOperators());
		functionFactory.add(new AtFunctionT());
		return new AtFormulaParser(formatter, functionFactory);
	}

	@Override
	public void run() {
		try {
			System.out.println("Please type a Lotus domino @formula. Quit with CTRL+Z:");
			SimpleNode n = null;
			List<Object> v = null;

			AtFormulaParser parser = getParser();
			parser.ReInit(System.in);
			n = parser.Parse();
			n.dump("");
			FormulaContext ctx = new FormulaContext(null, parser.getFormatter());
			v = n.evaluate(ctx);

			System.out.println("NTF:\t" + v);

			StringBuilder sb = new StringBuilder();
			n.toFormula(sb);
			System.out.println("Notes...: " + sb.toString());
			Session sess = Base.toLotus(Factory.getSession());
			try {
				v = sess.evaluate(sb.toString());
				System.out.println("Domino:\t" + v);
			} catch (NotesException e) {
				e.printStackTrace();
			}

			System.out.println("Thank you.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Factory.terminate();
		System.out.println(Factory.dumpCounters(true));
	}

}
