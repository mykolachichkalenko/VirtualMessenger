import Particles from "../../designedComponents/BackGroundForChat.jsx";
import AppBar from "./componentsForChat/AppBar.jsx";
import Sending from "./componentsForChat/Sending.jsx";
import Messages from "./componentsForChat/Messages.jsx";
import {useEffect, useRef, useState} from "react";
import axios from "axios";

export default function ChatWithChat({phone, myPhone,setHidden,isHidden}) {
    const [message, setMessage] = useState("");
    const [chat, setChat] = useState(null);
    const socketRef = useRef(null);
    const [messageSent,setMessageSent] = useState(null);
    const [messageForEdit,setMessageForEdit] = useState(null);
    const socketEditing = useRef(null);
    const [editedAndCorrectMessage,setEditedAndCorrectMessage] = useState(null);
    const reloadRef = useRef(null);

    //getting chat information
    useEffect(() => {
        axios.get(`http://localhost:8080/chat/with/${phone}`,
            {withCredentials: true}).then(resp => setChat(resp.data));
    }, []);
    //setting messages red
    useEffect(() => {
        if (!chat || !phone) return;
        axios.post("http://localhost:8080/messages/mark-as-red",{
            shard:chat.shard_of_chat.toString(),
            chatId:chat.id.toString(),
            phoneUser:phone
        },{withCredentials:true}).then();
    }, [chat,phone]);
    //websocket creation for sending messages
    useEffect(() => {
        if (!myPhone) return;

        const socket = new WebSocket(`ws://localhost:8080/ws/chat/message?phone=${myPhone}`);
        socketRef.current = socket;

        return () => {
            console.log("websocket closed")
            socket.close();
        };
    }, [myPhone]);
    //websocket for editing message
    useEffect(() => {
        if (!myPhone) return;

        socketEditing.current = new WebSocket(`ws://localhost:8080/ws/chat/message/edit?phone=${myPhone}`);

    }, [myPhone]);
    //websocket for reloading
    useEffect(() => {
        if (!myPhone) return;

        const socket = new WebSocket(`ws://localhost:8080/ws/chat/reload?phone=${myPhone}`);
        reloadRef.current = socket;

        socket.onmessage = (event) =>{
            const data = JSON.parse(event.data);
            if (data.message === "reload"){
                window.location.reload();
            }
        }
    }, [myPhone]);
    return (
        <div className={`w-screen h-screen overflow-hidden flex relative bg-black ${!isHidden ? "max-lg:hidden" : ""}`}>
            <Particles
                className="absolute top-0 right-0 w-full h-full z-0"
                particleColors={['#ffffff', '#ffffff']}
                particleCount={200}
                particleSpread={10}
                speed={0.1}
                particleBaseSize={100}
                moveParticlesOnHover={true}
                alphaParticles={false}
                disableRotation={false}
            />
            <div className={"w-full h-[6.5%] bg-[#0d0d0d] fixed z-10"}>
                <AppBar phone={phone} message={message} myPhone={myPhone} setHidden={setHidden}/>
            </div>
            <div
                className={"w-full h-full bg-transparent absolute flex items-center justify-center top-[0%] z-0 overflow-y-scroll "}>
                <Messages phone={phone} chat={chat} socketMessage={socketRef} messageSent={messageSent}
                          updatedMessage={editedAndCorrectMessage} myPhone={myPhone}
                          setMessageForEditP={setMessageForEdit}
                          setUpdatedMessage={setEditedAndCorrectMessage} socketForEditedMessage={socketEditing}
                          socketReload={socketRef}/>
            </div>
            <div
                className="w-[100%] h-[5%] flex justify-center items-center absolute bottom-0 z-20 bg-transparent max-lg:w-screen">
                <Sending onMessageChange={setMessage} chat={chat} sendingSocket={socketRef} myPhone={myPhone}
                         receiverPhone={phone} messageForEdit={messageForEdit} socketEditing={socketEditing}
                         setOnSend={setMessageSent} setMessageForEdit={setMessageForEdit}
                         setEditedAndCorrectMessage={setEditedAndCorrectMessage} reloadSocket={reloadRef}/>
            </div>
        </div>
    )
}
