package com.bosshi.maeul.websocket;

import com.bosshi.maeul.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. 브로드캐스트 토픽 프리픽스
        config.enableSimpleBroker("/sub");

        // 2. 클라이언트 -> 서버 메시지 송신 프리픽스
        config.setApplicationDestinationPrefixes("/pub");

        // 3. ★ 특정 사용자(convertAndSendToUser) 전용 프리픽스 변경 (기본값: "/user")
        // 예시로 "/private"으로 변경해 봅니다.
        config.setUserDestinationPrefix("/private");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 (SockJS 지원)
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // ★ 웹소켓 연결 시(CONNECT) 토큰을 검증하여 Principal을 설정하는 인터셉터 등록
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // 클라이언트가 연결을 시도할 때 (CONNECT 프레임)
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        try {
                            // 1. 토큰 유효성 검증 (프로젝트에 맞는 메서드 사용)
                            if (jwtTokenProvider.validateToken(token)) {
                                // 2. 토큰에서 유저 ID(또는 식별자) 추출
                                String userId = jwtTokenProvider.getUserIdFromToken(token);

                                // 3. Spring Security 인증 객체 생성 후 세션(Principal)에 등록
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(userId, null, null);

                                accessor.setUser(authentication);
                                log.info("[WebSocket 인증 성공] User ID: {}", userId);
                            } else {
                                log.warn("[WebSocket 인증 실패] 유효하지 않은 토큰입니다.");
                            }
                        } catch (Exception e) {
                            log.error("[WebSocket 인증 오류] 토큰 파싱 실패: {}", e.getMessage());
                        }
                    } else {
                        log.warn("[WebSocket 인증 실패] Authorization 헤더가 없거나 형식이 잘못되었습니다.");
                    }
                }
                return message;
            }
        });
    }
}