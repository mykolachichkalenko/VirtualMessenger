import {Button, Dropdown, DropdownItem, DropdownMenu, DropdownTrigger} from "@heroui/react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBars} from "@fortawesome/free-solid-svg-icons";
import {useState} from "react";
import ConfirmForm from "../../../designedComponents/ConfirmForm.jsx";
import LogOut from "./componentsSettingDropDown/LogOut.jsx";

export default function SettingsDropDown() {
    const [logOutOpen, setLogOutOpen] = useState(false);

    const selection = (key) => {
        if (key === "logOut") {
            setLogOutOpen(true);
        }
    }

    return (
        <div className={"relative top-2.5 2xl:right-1.5 max-2xl:left-2 lg:right-3"}>
            <Dropdown className={"bg-black/70"}>
                <DropdownTrigger>
                    <Button variant="light"><FontAwesomeIcon icon={faBars} size={"2x"}/></Button>
                </DropdownTrigger>
                <DropdownMenu aria-label="Action event example" onAction={(key) => selection(key)}>
                    <DropdownItem
                        className="text-white hover:bg-gray-700 transition"
                        onClick={() => window.location.href = "/profile"}
                    >
                        Profile
                    </DropdownItem>

                    <DropdownItem
                        key="new"
                        className="text-white hover:bg-gray-700 transition"
                        onClick={() => window.location.href = "/premium"}
                    >
                        VM Premium
                    </DropdownItem>

                    <DropdownItem
                        key="logOut"
                        className="text-white bg-gradient-to-r from-red-600 to-red-400 hover:from-red-700 hover:to-red-500 transition"
                    >
                        Log out
                    </DropdownItem>
                </DropdownMenu>

            </Dropdown>
            <LogOut openIn={logOutOpen} onClose={() => setLogOutOpen(false)} title={"LOG OUT"} text={"Do you really want to log out?"}/>
        </div>
    )
}