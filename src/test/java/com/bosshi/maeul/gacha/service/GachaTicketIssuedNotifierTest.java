package com.bosshi.maeul.gacha.service;

import com.bosshi.maeul.gacha.GachaTicketIssuedEvent;
import com.bosshi.maeul.gacha.TicketEventType;
import com.bosshi.maeul.gacha.response.GachaTicketIssuedMessage;
import com.bosshi.maeul.websocket.MessageSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.messaging.MessagingException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("GachaTicketIssuedNotifier 가챠권 발급 알림 테스트")
class GachaTicketIssuedNotifierTest {

    private static final GachaTicketIssuedEvent EVENT = new GachaTicketIssuedEvent(
            3L, 10L, TicketEventType.PHOTO_MISSION, LocalDateTime.of(2026, 12, 28, 12, 0));

    private final MessageSender messageSender = mock(MessageSender.class);

    @Configuration
    @EnableTransactionManagement
    static class Config {
        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build());
        }

        @Bean
        GachaTicketIssuedNotifier notifier(MessageSender messageSender) {
            return new GachaTicketIssuedNotifier(messageSender);
        }
    }

    private AnnotationConfigApplicationContext context() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(MessageSender.class, () -> messageSender);
        context.register(Config.class);
        context.refresh();
        return context;
    }

    @Test
    @DisplayName("커밋된 뒤에만 그 사용자에게 code 없는 페이로드를 보낸다")
    void sendsOnlyAfterCommit() {
        try (AnnotationConfigApplicationContext context = context()) {
            TransactionTemplate tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));

            tx.executeWithoutResult(status -> {
                context.publishEvent(EVENT);
                verifyNoInteractions(messageSender);
            });
            verify(messageSender).sendToUser(3L, "gacha-ticket-issued", GachaTicketIssuedMessage.from(EVENT));

            reset(messageSender);
            tx.executeWithoutResult(status -> {
                context.publishEvent(EVENT);
                status.setRollbackOnly();
            });
            verifyNoInteractions(messageSender);
        }
    }

    @Test
    @DisplayName("전송이 실패해도 예외를 밖으로 던지지 않는다")
    void swallowsSendFailure() {
        doThrow(new MessagingException("fail")).when(messageSender).sendToUser(anyLong(), anyString(), any());

        new GachaTicketIssuedNotifier(messageSender).onTicketIssued(EVENT);

        verify(messageSender).sendToUser(anyLong(), anyString(), any());
    }
}
