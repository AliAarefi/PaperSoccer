package papersoccer.common;

public class ServerMessage {
	public static final String authentication_approved = "$S3";
	public static final String authentication_failed = "$S4";
	public static final String error_players_full = "$S0";
	public static final String world_broadcast = "$S1";
	public static final String turn_broadcast = "$S2";
	public static final String ball_position_broadcast = "$S14";
	public static final String join_accepted = "$S8";
	public static final String join_failed = "$S9";
	public static final String turn_of_bottom_player = "$S12";
	public static final String turn_of_upper_player = "$S13";
	public static final String action_accepted = "$S6";
	public static final String action_failed = "$S7";
	public static final String leave_accepted = "$S10";
	public static final String leave_failed = "$S11";
	public static final String game_paused = "$S15";
	public static final String game_finished = "$S5";

}
