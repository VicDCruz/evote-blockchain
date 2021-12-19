// Next.js API route support: https://nextjs.org/docs/api-routes/introduction
import { Gateway, Wallets } from 'fabric-network';
import * as path from 'path';
import * as fs from 'fs';

/**
 * 
 * @returns wallet:Wallet
 */
async function getWallet() {
  const walletPath = path.join(process.cwd(), 'Org1Wallet');
  const wallet = await Wallets.newFileSystemWallet(walletPath);
  console.log(`Wallet path: ${walletPath}`);
  return wallet;
}

/**
 * 
 * @param {Wallets} wallet 
 * @returns 
 */
async function getGateway(wallet) {
  const gateway = new Gateway();
  const connectionProfilePath = path.resolve(__dirname, '../../../../../', 'connection.json');
  const connectionProfile = JSON.parse(fs.readFileSync(connectionProfilePath, 'utf8'));
  const connectionOptions = { wallet, identity: 'Org1 Admin', discovery: { enabled: true, asLocalhost: true } };
  await gateway.connect(connectionProfile, connectionOptions);
  return gateway;
}

export default async function handler(req, res) {
  const { slug } = req.query;
  const wallet = await getWallet();
  const gateway = await getGateway(wallet);
  const network = await gateway.getNetwork('mychannel');
  const contract = network.getContract('evote');
  const output = await contract.submitTransaction(slug, ...Object.values(req.body));
  gateway.disconnect();
  res.status(200).json({ output: output.toString() })
}
