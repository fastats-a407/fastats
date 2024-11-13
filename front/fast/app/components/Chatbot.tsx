"use client";

import React, { useEffect, useState } from "react";
import axios from "axios";

const apiUrl = process.env.NEXT_PUBLIC_API_URL;

const axiosInstance = axios.create({
  baseURL: `${apiUrl}/chatbot`,
  withCredentials: true, // 모든 요청에 쿠키 포함 설정
});

// 메시지 타입 정의
type Message = {
  sender: string;
  text: string;
};

// EventSource 인스턴스를 외부에서 관리하여 단일 연결 유지
let eventSource: EventSource | null = null;

export default function Chatbot() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [currentMessage, setCurrentMessage] = useState(""); // 현재 입력 중인 메시지 상태

  useEffect(() => {
    // 챗봇 창이 열릴 때만 EventSource를 설정
    if (isOpen && !eventSource) {
      eventSource = new EventSource(`${axiosInstance.defaults.baseURL}/stream` , {withCredentials : true});
    

      eventSource.onmessage = (event: MessageEvent) => {
        // 실시간으로 메시지를 한 글자씩 추가
        setCurrentMessage((prev) => prev + event.data);
      };

      // 서버에서 'complete' 이벤트를 받을 때 처리
      eventSource.addEventListener("complete", () => {
        setMessages((prev) => [...prev, { sender: "bot", text: currentMessage }]);
        setCurrentMessage(""); // 현재 메시지를 초기화하여 완료 표시
      });

      // 에러가 발생하면 연결을 닫고 eventSource를 null로 설정
      eventSource.onerror = () => {
        eventSource?.close();
        eventSource = null; // 다음에 연결할 수 있도록 null로 설정
      };
    }

    // 컴포넌트가 언마운트될 때 EventSource를 닫음
    return () => {
      eventSource?.close();
      eventSource = null;
    };
  }, [isOpen]);

  // 챗봇 창 열기/닫기
  const toggleChatbot = () => {
    if (isOpen) {
      endStream();
      eventSource?.close(); // 현재 연결을 닫고 eventSource를 null로 설정
      eventSource = null;
    }
    setIsOpen(!isOpen);
  };

  // 서버에 메시지 전송
  const sendMessage = async () => {
    if (input.trim()) {
      setMessages((prev) => [...prev, { sender: "user", text: input }]); // 사용자 메시지 화면에 표시

      // try {
      //   // 기존 EventSource 연결은 유지하면서 메시지 전송만 수행
      //   await axiosInstance.post("/message", { message: input });
      //   setInput("");
      // } catch (error) {
      //   console.error("Failed to send message:", error);
      // }
      try {
        // 메시지 전송 수행
        const response = await fetch(`${apiUrl}/chatbot/message`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ message: input }),
          credentials: "include", // 쿠키 자동 포함 설정
        });
        
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
  
        setInput("");
      } catch (error) {
        console.error("Failed to send message:", error);
      }
    }
  };

  // SSE 연결 종료 요청
  const endStream = async () => {
    // try {
    //   await axiosInstance.get("/end");
    // } catch (error) {
    //   console.error("Failed to end stream:", error);
    // }
    try {
      const response = await fetch(`${apiUrl}/chatbot/end`, {
        method: "GET",
        credentials: "include", // 쿠키 자동 포함 설정
      });
  
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
    } catch (error) {
      console.error("Failed to end stream:", error);
    }
  };


  return (
    <div style={{ position: "fixed", bottom: "20px", right: "20px", zIndex: 1000 }}>
      {isOpen ? (
        <div className="chatbot-window">
          <button onClick={toggleChatbot}>Close Chat</button>
          <div className="chat-content">
            <div className="messages">
              {messages.map((msg, index) => (
                <div key={index} className={msg.sender === "bot" ? "bot-message" : "user-message"}>
                  {msg.text}
                </div>
              ))}
              {currentMessage && <div className="bot-message typing">{currentMessage}</div>}
            </div>
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type a message..."
            />
            <button onClick={sendMessage}>Send</button>
          </div>
        </div>
      ) : (
        <button onClick={toggleChatbot}>Open Chat</button>
      )}
    </div>
  );
}
