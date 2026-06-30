package com.antonioteca.cc42.utility;

import java.util.function.Consumer;

public class RateLimiter {
    private long lastExecuted = 0;
    private final long cooldownMs;

    public RateLimiter(long cooldownMs) {
        this.cooldownMs = cooldownMs;
    }

    /**
     * Executa uma ação respeitando o intervalo de cooldown.
     *
     * @param callback     Ação a ser executada.
     * @param triggerToast Função que recebe a mensagem de erro.
     * @return true se executou, false se foi bloqueado pelo rate limit.
     */
    public boolean executeWithLimit(Runnable callback, Consumer<String> triggerToast) {
        long now = System.currentTimeMillis();
        long diff = now - lastExecuted;

        if (diff < cooldownMs) {
            long remainingSeconds = (long) Math.ceil((cooldownMs - diff) / 1000.0);

            if (triggerToast != null) {
                triggerToast.accept("Aguarde " + remainingSeconds + "s antes de actualizar novamente.");
            }
            return false;
        }

        lastExecuted = now;
        if (callback != null) {
            callback.run();
        }
        return true;
    }
}
