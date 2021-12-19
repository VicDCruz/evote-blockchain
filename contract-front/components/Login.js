import React, { useState } from 'react'
import PropTypes from 'prop-types'
import { useRouter } from 'next/router';

const Login = props => {
    const [id, setId] = useState("");
    const router = useRouter();

    const handleClick = () => {
        if (id) {
            router.push('/dashboard/' + id);
        }
    };

    return (
        <div className="space-y-4">
            <div className="flex flex-col space-y-1">
                <span className="font-semibold text-sky-800 text-lg">ID</span>
                <input type="text" onChange={setId} />
            </div>
            <button
                type="button"
                className="bg-gradient-to-r from-sky-500 to-indigo-500 px-4 py-2 text-white font-bold tracking-widest hover:opacity-70"
                onClick={handleClick}
            >
                Enviar
            </button>
        </div>
    )
}

Login.propTypes = {

}

export default Login
