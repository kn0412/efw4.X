/**** efw4.X Copyright 2019 efwGrp ****/
package efw;
/**
 * サブアプリとして定義していないエラー。
 * @author kejun.chang
 */
public final class efwDoNotAsSubException extends efwException {
	public efwDoNotAsSubException() {
		super("The calling is rejected because [efw.as.main] is not false.");
	}
}
