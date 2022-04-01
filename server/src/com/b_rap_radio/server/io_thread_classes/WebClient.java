package com.b_rap_radio.server.io_thread_classes;

import com.b_rap_radio.server.admin_server.TCPAdminHandler;
import com.b_rap_radio.server.dataclasses.*;
import com.b_rap_radio.server.dataclasses.request_related.*;
import com.google.gson.JsonParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.Socket;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebClient implements Runnable {

	static final List<WebClient> activeWebClients = Collections.synchronizedList(new ArrayList<>());
	
	public WebClient(Socket s) {
		System.out.println("new web interface instantiated !");
		this.s = s;
		this.handlerPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		handlerPool.setKeepAliveTime(2, TimeUnit.DAYS); // to eliminate any radio reflection
		rsaConverter = RSAConverter.getInstance();
	}

	@Override
	public void run() {
		activeWebClients.add(this);
		System.out.println("running..");

		PrintWriter output;
		BufferedReader input;
		try {
			output = new PrintWriter(s.getOutputStream());
			input = new BufferedReader(new InputStreamReader(s.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		while(this.running && !this.s.isClosed()) {
			try {
				Request r;
				String strReq = input.readLine();

				try {
					r = JSONConverter.getRequestFromJSON(String.valueOf(strReq));
				} catch (JsonParseException e) {
					// TODO LOG
					continue;
				}

				final String type;
				try {
					type = r.getType();
				} catch (NullPointerException e) { // input.readLine() returning shit (r == null), means client just disconnected
					break;
				}

				System.out.println("request received !");
				System.out.println("type -> ".concat(r.getType()));

				if (type.equalsIgnoreCase("login")) {

					List<String> args = r.getStringArgs();
					if (args != null) {

						if (args.size() >= 2) {

							String id, pwd;

							try {
								id = rsaConverter.decodeText(args.get(0));
								pwd = rsaConverter.decodeText(args.get(1));

								if(id.startsWith("ID:") && pwd.startsWith("PWD:")){

									Request serverAns = DatabaseAPI.loginRequest(id.substring(3), pwd.substring(4));

									if(serverAns.getUserArgs() != null && !serverAns.isError()){ // userArgs exists (-> contains smth ; user info -> logged)
										UserEntity userEntity = serverAns.getUserArgs().get(0);
										List<SanctionEntity> sanctions = serverAns.getSanctionArgs();
										// List<SanctionEntity> currentSanctions = new ArrayList<>();
										boolean banned = false;

										if (sanctions == null)
											sanctions = new ArrayList<>();

										for (SanctionEntity e : sanctions) {

											if (!e.hasEnded()) {

												//currentSanctions.add(e);

												if ("BANNED".equals(e.getType())) {
													banned = true;
												}

											}
										}

										// TODO ADD TO USER OBJ ?


										if (!banned) {
											serverAns.setEventArgs(EventList.getInstance().getEventList());
											output.println(JSONConverter.getJSONFromRequest(serverAns));
											output.flush();
											RadioUser user = new RadioUser(this.s, userEntity.getEmail(),
													userEntity.getNickname(), userEntity.getCredits(), userEntity.getStatus());
											handlerPool.execute(new TCPHandler(user));
											UserList.getInstance().add(user);
											activeWebClients.remove(this);
											return;

										} else {
											output.println(JSONConverter.getJSONFromRequest(new Request("LOGIN", "BANNED")));
										}
									} else {
										output.println(JSONConverter.getJSONFromRequest(serverAns));
									}

								} else {
									output.println(JSONConverter.getJSONFromRequest(new Request("LOGIN", "BAD ENCODE")));
								}

							} catch (BadPaddingException | IllegalBlockSizeException e) {
								e.printStackTrace();
								output.println(JSONConverter.getJSONFromRequest(new Request("LOGIN", "SERVER ERROR")));
							}


						} else {
							output.println(JSONConverter.getJSONFromRequest(new Request("LOGIN", "MISSING ARGS")));
						}

					} else {
						output.println(JSONConverter.getJSONFromRequest(new Request("LOGIN", "MISSING ARGS")));
					}

					output.flush();


				} else if (type.equalsIgnoreCase("signup")) {

					if (r.getStringArgs() != null) {

						List<String> args = r.getStringArgs();

						if (args.size() >= 3) {

							Request serverAns = DatabaseAPI.registerRequest(args.get(0), args.get(1), args.get(2));

							if (!serverAns.isError()) {

								if (serverAns.getStringArgs() != null && "SIGNUP".equals(serverAns.getType())) {

									if (serverAns.getStringArgs().size() > 0) {

										output.println(JSONConverter.getJSONFromRequest(serverAns));

										// ping current admins
										new Thread(() -> {
											final Request apiAns = DatabaseAPI.getUserInfos(args.get(1));
											if(!apiAns.isError() && apiAns.getUserArgs() != null) {
												try {
													final Request ping = new Request(RequestTypes.ADD_USER.getName());
													ping.addUserArgs(apiAns.getUserArgs().get(0));
													new TCPAdminHandler.GeneralQuery(ping, false, null);
												} catch (IndexOutOfBoundsException ignored) { } // TODO log ?
											} // TODO log ?
										}).start();

									} else {
										output.println(JSONConverter.getJSONFromRequest(new Request("SIGNUP", "SERVER ERROR")));
										// TODO LOG
									}

								} else {
									output.println(JSONConverter.getJSONFromRequest(new Request("SIGNUP", "SERVER ERROR")));
									// TODO LOG
								}

							} else {
								output.println(JSONConverter.getJSONFromRequest(serverAns));
							}

						} else {
							output.println(JSONConverter.getJSONFromRequest(new Request("SIGNUP", "MISSING ARGS")));
						}

					} else {
						output.println(JSONConverter.getJSONFromRequest(new Request("SIGNUP", "MISSING ARGS")));
					}

					output.flush();


				} else if (type.equalsIgnoreCase("get_key")) {
					Request request = new Request(type);
					RSAPublicKey key = rsaConverter.getPublicKey();
					request.addStringArgs(key.getModulus().toString(), key.getPublicExponent().toString());
					output.println(JSONConverter.getJSONFromRequest(request));
					output.flush();
				} else {
					System.out.println("unknown command spotted");
					output.println(JSONConverter.getJSONFromRequest(new Request("ERROR", "UNKNOWN CMD")));
					output.flush();
				}

			} catch (IOException | JsonParseException e) {
				System.out.println("Exception spotted (io or jsonparse) :");
				e.printStackTrace();
				System.out.println("END OF EXCEPTION");
				// TODO LOG
				break;
			}
		}

		activeWebClients.remove(this);
		output.close();

		try {
			input.close();
		} catch (IOException ignored) { }

		try {
			s.close();
		} catch (IOException ignored) { }
	}

	public void shutdown(){
		this.running = false;
		handlerPool.shutdown();
	}
	
	
	private final Socket s;
	private boolean running = true;
	private final ThreadPoolExecutor handlerPool;
	private final RSAConverter rsaConverter;
}
