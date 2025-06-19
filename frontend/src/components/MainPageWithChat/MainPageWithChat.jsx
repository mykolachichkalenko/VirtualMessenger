import UsersSearchETCWithChat from "./componentsForMainPageWithChat/UsersSearchETCWithChat.jsx";
import ChatWithChat from "./componentsForMainPageWithChat/ChatWithChat.jsx";
import {useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";

export default function MainPageWithChat() {
    const {phone} = useParams();
    const [myPhone, setMyPhone] = useState("");
    const [isHidden, setIsHidden] = useState(true);

    useEffect(() => {
        axios.get("http://localhost:8080/get/my/phone", {withCredentials: true})
            .then(resp => setMyPhone(resp.data));

        axios.get("http://localhost:8080/get/set/online", {withCredentials: true}).then();
    }, []);
    useEffect(() => {
        if (!phone || !myPhone) return;

        axios.post("http://localhost:8080/chat/set/red/me", {receiverPhone: myPhone, senderPhone: phone}
            , {withCredentials: true}).then()
    }, [phone, myPhone]);

    return (
        <div className={"h-screen w-screen flex justify-center items-center"}>
            <div className={` bg-white  w-screen h-screen flex justify-items-center`}>
                <div
                    className={`h-screen bg-danger lg:w-1/2 2xl:w-1/4 max-lg:w-screen ${isHidden ? "max-lg:hidden" : "max-lg:block"}`}>      {/*Chats,etc.*/}
                    <UsersSearchETCWithChat myPhone={myPhone} phone={phone}/>
                </div>
                {/*Chat With User*/}

                    <ChatWithChat phone={phone} myPhone={myPhone} setHidden={setIsHidden} isHidden={isHidden}/>

            </div>
        </div>
    )
}