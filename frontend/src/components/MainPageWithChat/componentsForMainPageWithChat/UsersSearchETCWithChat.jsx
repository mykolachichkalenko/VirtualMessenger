import SettingsDropDownWithChat from "./componentsForUserSearchETC/SettingsDropDownWithChat.jsx";
import SearchWithChat from "./componentsForUserSearchETC/SearchWithChat.jsx";
import Chats from "./componentsForUserSearchETC/ChatsWithChat.jsx";
import ChatsWithChat from "./componentsForUserSearchETC/ChatsWithChat.jsx";

export default function UsersSearchETCWithChat({myPhone,phone}){
    return(
        <div className={"w-full h-full bg-[#0d0d0d]"}>
            <div className={"w-full justify-end flex relative top-1"}>
                <SettingsDropDownWithChat/>
                <SearchWithChat/>
            </div>
            <div className={"relative left-1 top-[2.7%] h-auto w-[98%]"}>
                <ChatsWithChat myPhone={myPhone} phone={phone}/>
            </div>
        </div>
    )
}