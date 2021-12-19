import React, { useState, useEffect } from 'react'
import axios from 'axios';
import { useRouter } from 'next/router';
import VoteForm from '../../components/VoteForm';
import VerifyReceiptForm from '../../components/VerifyReceiptForm';
import CreateVoterForm from '../../components/CreateVoterForm';

const options = [
    { name: "Votar", url: "vote" },
    { name: "Verificar recibo de voto", url: "verifyReceipt" },
    { name: "Ver última elección abierta", url: "getLatestElection" },
];

const adminOptions = [
    { name: "Crear votante", url: "createVoter" },
    { name: "Inicializar datos", url: "instantiate" },
    { name: "Cerrar elección", url: "closeLatestElection" },
    { name: "Contar votos", url: "countVotes" },
];

const Dashboard = () => {
    const router = useRouter();
    const [user, setUser] = useState({});
    const { id } = router.query;
    useEffect(() => {
        if (Object.keys(user).length === 0)
            axios.post(`/api/submit/getUser`, { id })
                .then(res => setUser(JSON.parse(res.data.output)))
                .catch(error => console.error(error));
    }, [id, user]);
    const [output, setOutput] = useState(null);
    const [formShowing, setFormShowing] = useState("");
    const handleClick = slug => () => {
        switch (slug) {
            case 'instantiate':
            case 'closeLatestElection':
                axios.get(`/api/submit/${slug}`).then(res => setOutput(res.data.output));
                break;
            case 'countVotes':
            case 'getLatestElection':
                axios.get(`/api/evaluate/${slug}`).then(res => setOutput(res.data.output));
                break;
            case 'vote':
            case 'verifyReceipt':
            case 'createVoter':
                setFormShowing(slug)
                break;
            default:
                break;
        }
    };

    return (
        <div className="w-full h-screen flex justify-center py-4">
            <div className="w-3/4 space-y-6">
                <div className="font-bold text-4xl border-b-2 border-emerald-300 pb-2 px-2">
                    Dashboard - {user.firstName}
                </div>
                <div className="flex flex-row gap-2">
                    <div className="w-4/6 flex flex-col gap-4 ">
                        <div className="">
                            <p className="font-medium text-2xl text-emerald-800">Opciones del votante</p>
                            <div className="grid grid-cols-3 grid-flow-row gap-4">
                                {options.map(option => (
                                    <button
                                        key={option.url}
                                        type="button"
                                        className="bg-emerald-200 px-2 py-4 text-center"
                                        onClick={handleClick(option.url)}
                                    >
                                        {option.name}
                                    </button>
                                ))}
                            </div>
                        </div>
                        <div className="">
                            <p className="font-medium text-2xl text-orange-800">Opciones del Administrador</p>
                            <div className="grid grid-cols-3 grid-flow-row gap-4">
                                {adminOptions.map(option => (
                                    <button
                                    key={option.url}
                                    type="button"
                                    className="bg-orange-200 px-2 py-4 text-center"
                                    onClick={handleClick(option.url)}
                                    >
                                        {option.name}
                                    </button>
                                ))}
                            </div>
                        </div>
                    </div>
                    <div className="w-2/6 bg-sky-200 p-2">
                        {formShowing === 'vote' && <VoteForm setOutput={setOutput} voterId={user.id} />}
                        {formShowing === 'verifyReceipt' && <VerifyReceiptForm setOutput={setOutput} />}
                        {formShowing === 'createVoter' && <CreateVoterForm setOutput={setOutput} />}
                    </div>
                </div>
                <div className="space-y-2">
                    <p className="font-medium text-2xl text-zinc-800">Salida</p>
                    <div className="bg-zinc-500 h-44 px-4 py-6 text-zinc-100 font-mono">
                        {output || "Sin resultados"}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Dashboard
