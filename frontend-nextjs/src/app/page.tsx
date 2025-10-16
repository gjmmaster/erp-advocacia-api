import { redirect } from 'next/navigation';

export default function Home() {
  // Redireciona para a página de login do super admin
  redirect('/super-admin/login');
}
