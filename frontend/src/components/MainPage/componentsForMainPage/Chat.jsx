import Particles from "../../designedComponents/BackGroundForChat.jsx";

export default function Chat() {

    return (

        <div className="w-3/4 h-screen flex relative right-0 bg-black max-lg:hidden">
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
        </div>
    )
}