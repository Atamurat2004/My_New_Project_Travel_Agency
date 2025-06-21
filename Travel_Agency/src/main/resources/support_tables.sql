-- Создание таблицы тикетов поддержки
CREATE TABLE IF NOT EXISTS support_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    booking_id BIGINT,
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    ticket_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    admin_notes TEXT,
    refund_requested BOOLEAN DEFAULT FALSE,
    refund_approved BOOLEAN DEFAULT FALSE,
    refund_date TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Создание таблицы сообщений поддержки
CREATE TABLE IF NOT EXISTS support_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    support_ticket_id BIGINT NOT NULL,
    sender_id BIGINT,
    message TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message_type VARCHAR(20) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (support_ticket_id) REFERENCES support_tickets(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- Создание индексов для оптимизации
CREATE INDEX IF NOT EXISTS idx_support_tickets_user_id ON support_tickets(user_id);
CREATE INDEX IF NOT EXISTS idx_support_tickets_status ON support_tickets(status);
CREATE INDEX IF NOT EXISTS idx_support_tickets_ticket_type ON support_tickets(ticket_type);
CREATE INDEX IF NOT EXISTS idx_support_tickets_refund_requested ON support_tickets(refund_requested);
CREATE INDEX IF NOT EXISTS idx_support_messages_ticket_id ON support_messages(support_ticket_id);
CREATE INDEX IF NOT EXISTS idx_support_messages_sent_at ON support_messages(sent_at); 