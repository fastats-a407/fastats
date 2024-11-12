"use client";

import { useState } from "react";

export default function Chatbot() {
    const [isOpen, setIsOpen] = useState(false);
  
    const toggleChatbot = () => {
      setIsOpen(!isOpen);
    };
  
    return (
      <div style={{ position: 'fixed', bottom: '20px', right: '20px', zIndex: 1000 }}>
        {isOpen ? (
          <div className="chatbot-window">
            <button onClick={toggleChatbot}>Close Chat</button>
            <div className="chat-content">
              {/* 여기에 채팅봇의 내용을 구현 */}
              <p>Chatbot is here to help!</p>
            </div>
          </div>
        ) : (
          <button onClick={toggleChatbot}>Open Chat</button>
        )}
      </div>
    );
  }