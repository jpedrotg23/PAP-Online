package com.br.unifor.paponline.config;

public final class SessionContext {

    private static final ThreadLocal<SessionInfo> CTX = new ThreadLocal<>();

    private SessionContext() {}

    public static void set(Long userId, String perfil) {
        CTX.set(new SessionInfo(userId, perfil));
    }

    public static SessionInfo get() { return CTX.get(); }

    public static void clear() { CTX.remove(); }

    public record SessionInfo(Long userId, String perfil) {}
}