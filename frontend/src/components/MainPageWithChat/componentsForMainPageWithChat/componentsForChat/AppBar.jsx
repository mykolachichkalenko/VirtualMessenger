import {useEffect, useRef, useState} from "react";
import axios from "axios";
import {User} from "@heroui/react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faXmark} from "@fortawesome/free-solid-svg-icons";

export default function AppBar({phone, message,myPhone,setHidden}) {
    const [userChat, setUserChat] = useState(null);
    const [isTyping, setIsTyping] = useState(false);
    const socketRef = useRef(null);
    const timeoutRef = useRef(null);

    useEffect(() => {
        if (!myPhone) return;
        axios.get(`http://localhost:8080/get/user/${phone}`, {withCredentials: true}).then(res => setUserChat(res.data));

        const socket = new WebSocket(`ws://localhost:8080/ws/chat/isTyping?phone=${myPhone}`);
        socketRef.current = socket;

        socket.onmessage = (event) => {
            const data = JSON.parse(event.data);
            if (data.type === true && data.fromUserPhone === phone) {
                setIsTyping(true);
                clearTimeout(timeoutRef.current);
                timeoutRef.current = setTimeout(() => setIsTyping(false), 1500);
            }
        };

        return () => {
            socket.close();
            clearTimeout(timeoutRef.current);
        };
    }, [myPhone]);

    useEffect(() => {
        if (message) {
            if (socketRef.current?.readyState === WebSocket.OPEN) {
                const typingMessage = {
                    fromUserPhone: myPhone,
                    toUserPhone: phone,
                    type: true,
                };
                socketRef.current.send(JSON.stringify(typingMessage));

            }
        }
    }, [message]);

    return (
        <div>
            {userChat &&
                <User className={"relative left-6 top-3.5"}
                      name={<span className={"text-3xl relative bottom-1 left-2"}>{userChat.name}</span>}
                      description={isTyping ? <span className={"text-lg relative bottom-1 left-2"}>typing...</span> :
                          userChat.lastSeen ?
                              <span className={"text-lg relative bottom-1 left-2"}>{userChat.lastSeen}</span> :
                              <span className={"text-lg relative bottom-1 left-2"}>offline</span>}
                      avatarProps={{
                          size: "lg",
                          src: userChat.avatarUrl
                      }}/>}
            <FontAwesomeIcon icon={faXmark} size={"2xl"} className={"absolute top-5 right-5"} onClick={() => setHidden(false)}/>
        </div>
    )
}