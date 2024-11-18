"use client";

import React, { useEffect, useState } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";


const apiUrl = process.env.NEXT_PUBLIC_API_URL;

const axiosInstance = axios.create({
  baseURL: `${apiUrl}/chatbot`,
  withCredentials: true,
});


type Message = {
  sender: string;
  text: string;
  keywords?: string[]; // 키워드를 배열로 추가
};


let eventSource: EventSource | null = null;
let messageBuffer = "";

export default function Chatbot() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [currentMessage, setCurrentMessage] = useState("");
  const router = useRouter();

  useEffect(() => {
    if (eventSource) {
      endStream();
    }

    if (isOpen && !eventSource) {
      eventSource = new EventSource(`${axiosInstance.defaults.baseURL}/stream`, { withCredentials: true });

      eventSource.onopen = () => {
        // console.log("SSE connected");
        setMessages((prevMessages) => [
          ...prevMessages,
          { sender: "bot", text: "연결이 되었습니다." },
        ]);
      };

      eventSource.onerror = () => {
        setMessages((prevMessages) => [
          ...prevMessages,
          { sender: "bot", text: "연결이 되지 않았습니다. 챗봇을 껐다 켜주세요." },
        ]);
  
        eventSource?.close();
        eventSource = null;
      };

      eventSource.onmessage = (event: MessageEvent) => {
        messageBuffer += event.data;
        console.log("Updated messageBuffer:", messageBuffer);
        setCurrentMessage((prev) => prev + event.data);
      };

      const handleComplete = () => {
        const keywords = messageBuffer.match(/\d+\.(\S+)/g)?.map((item) => item.replace(/\d+\./, ""));

        setMessages((prevMessages) => [
          ...prevMessages,
          {
            sender: "bot",
            text: messageBuffer,
            keywords, // 키워드를 하나의 메시지 객체에 배열로 추가
          },
        ]);

        setTimeout(() => {
          messageBuffer = "";
          setCurrentMessage("");
        }, 10);
      };

      eventSource.addEventListener("complete", handleComplete);

      eventSource.onerror = () => {
        eventSource?.close();
        eventSource = null; // 다음에 연결할 수 있도록 null로 설정
      };
    }

    return () => {
      endStream();
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

  useEffect(() => {
    console.log("Updated messages:", messages);
  }, [messages])

  const handleKeywordClick = (keyword: string) => {
    router.push(`/search/${encodeURIComponent(keyword)}`);
  };


  return (
    <div style={{ position: "fixed", bottom: "20px", right: "20px", zIndex: 1000 }}>
      {isOpen ? (
        <div className="chatbot-window">
          <button onClick={toggleChatbot}>Close Chat</button>
          <div className="chat-content">
            <div className="messages">
              {messages.map((msg, index) => (
                <div
                  key={index}
                  className={`message-item ${msg.sender === "bot" ? "bot-message" : "user-message"}`}
                >
                  {/* 텍스트 메시지 출력 */}
                  <span>{msg.text}</span>

                  {/* 키워드가 있으면 한 줄로 나열 */}
                  {msg.keywords && (
                    <div className="keywords-container">
                      {msg.keywords.map((keyword, i) => (
                        <span
                          key={i}
                          className="keyword-item"
                          onClick={() => handleKeywordClick(keyword)}
                        >
                          {keyword}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              ))}
              {currentMessage && (
                <div className="message-item bot-message">
                  {currentMessage}
                </div>
              )}
            </div>
          </div>
          <div className="chat-input-container">
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type a message..."
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  sendMessage();
                  setTimeout(()=>{
                    setInput("");
                  },10);
                }
              }
              }
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

