import Search from "./componentsForUSerSearchETC/Search.jsx";
import SettingsDropDown from "./componentsForUSerSearchETC/SettingsDropDown.jsx";
import Chats from "./componentsForUSerSearchETC/Chats.jsx";

export default function UsersSearchETC({myPhone}){
    return(
        <div className={"w-full h-full bg-[#0d0d0d]"}>
            <div className={"w-full justify-end flex relative top-1"}>
                <SettingsDropDown/>
                <Search/>
            </div>
            <div className={"relative left-1 top-[2.7%] h-auto w-[98%]"}>
                <Chats myPhone={myPhone}/>
            </div>
        </div>
    )
}