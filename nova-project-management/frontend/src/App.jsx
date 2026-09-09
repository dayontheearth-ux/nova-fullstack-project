import { useEffect, useState } from 'react'
import { Routes, Route, Navigate, Link, useNavigate, useParams } from 'react-router-dom'
import api from './api'

function Protected({children}) {
  return localStorage.getItem('nova_token') ? children : <Navigate to="/login" />
}

function Layout({children}) {
  const nav = useNavigate()
  const user = JSON.parse(localStorage.getItem('nova_user') || '{}')
  function logout(){ localStorage.clear(); nav('/login') }
  return <div className="app">
    <aside>
      <div className="brand">NOVA<span>.</span></div>
      <p className="tagline">Plan. Collaborate. Deliver.</p>
      <nav>
        <Link to="/">Dashboard</Link>
        <Link to="/projects">Projects</Link>
      </nav>
      <div className="side-bottom">
        <div className="avatar">{(user.name||'U')[0]}</div>
        <div><b>{user.name}</b><small>{user.email}</small></div>
        <button className="link-btn" onClick={logout}>Logout</button>
      </div>
    </aside>
    <main>{children}</main>
  </div>
}

function Auth({register=false}) {
  const nav=useNavigate()
  const [form,setForm]=useState({name:'',email:'',password:''})
  const [error,setError]=useState('')
  async function submit(e){
    e.preventDefault(); setError('')
    try {
      const url=register?'/auth/register':'/auth/login'
      const {data}=await api.post(url,form)
      localStorage.setItem('nova_token',data.token)
      localStorage.setItem('nova_user',JSON.stringify(data))
      nav('/')
    } catch(err){setError(err.response?.data || 'Something went wrong')}
  }
  return <div className="auth">
    <div className="auth-card">
      <div className="brand center">NOVA<span>.</span></div>
      <h1>{register?'Create account':'Welcome back'}</h1>
      <p>{register?'Start managing your team.':'Sign in to your workspace.'}</p>
      <form onSubmit={submit}>
        {register && <input placeholder="Full name" required value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/>}
        <input type="email" placeholder="Email" required value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/>
        <input type="password" placeholder="Password" required minLength="6" value={form.password} onChange={e=>setForm({...form,password:e.target.value})}/>
        {error && <div className="error">{String(error)}</div>}
        <button className="primary full">{register?'Create account':'Login'}</button>
      </form>
      <p className="switch">{register?'Already have an account?':'New to NOVA?'} <Link to={register?'/login':'/register'}>{register?'Login':'Create account'}</Link></p>
    </div>
  </div>
}

function Dashboard(){
  const [stats,setStats]=useState({projects:0,tasks:0,completedTasks:0,pendingTasks:0})
  const [projects,setProjects]=useState([])
  useEffect(()=>{api.get('/dashboard').then(r=>setStats(r.data));api.get('/projects').then(r=>setProjects(r.data))},[])
  const percent=stats.tasks?Math.round(stats.completedTasks/stats.tasks*100):0
  return <Layout><header><div><h1>Dashboard</h1><p>Your team's work at a glance.</p></div><Link className="primary" to="/projects">View projects</Link></header>
    <section className="stats">
      <Stat title="Projects" value={stats.projects}/>
      <Stat title="Total tasks" value={stats.tasks}/>
      <Stat title="Completed" value={stats.completedTasks}/>
      <Stat title="Pending" value={stats.pendingTasks}/>
    </section>
    <div className="grid2">
      <section className="panel"><h2>Overall progress</h2><div className="big-percent">{percent}%</div><div className="progress"><i style={{width:percent+'%'}}/></div><p>{stats.completedTasks} of {stats.tasks} tasks completed</p></section>
      <section className="panel"><h2>Projects</h2>{projects.length===0?<p>No projects yet.</p>:projects.slice(0,5).map(p=><Link className="project-row" to={'/projects/'+p.id} key={p.id}><div><b>{p.name}</b><small>{p.completedTasks}/{p.totalTasks} tasks</small></div><span>{p.totalTasks?Math.round(p.completedTasks/p.totalTasks*100):0}%</span></Link>)}</section>
    </div>
  </Layout>
}
function Stat({title,value}){return <div className="stat"><small>{title}</small><strong>{value}</strong></div>}

function Projects(){
 const [items,setItems]=useState([]), [show,setShow]=useState(false), [form,setForm]=useState({name:'',description:'',startDate:'',dueDate:''})
 const load=()=>api.get('/projects').then(r=>setItems(r.data))
 useEffect(load,[])
 async function create(e){e.preventDefault();await api.post('/projects',form);setForm({name:'',description:'',startDate:'',dueDate:''});setShow(false);load()}
 return <Layout><header><div><h1>Projects</h1><p>Organize your team's work.</p></div><button className="primary" onClick={()=>setShow(true)}>+ New project</button></header>
 {show&&<Modal title="Create project" close={()=>setShow(false)}><form onSubmit={create} className="form"><input placeholder="Project name" required value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/><textarea placeholder="Description" value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/><div className="form2"><input type="date" value={form.startDate} onChange={e=>setForm({...form,startDate:e.target.value})}/><input type="date" value={form.dueDate} onChange={e=>setForm({...form,dueDate:e.target.value})}/></div><button className="primary">Create project</button></form></Modal>}
 <div className="cards">{items.map(p=><Link className="project-card" to={'/projects/'+p.id} key={p.id}><div className="card-top"><span className="dot"/><span>{p.dueDate||'No deadline'}</span></div><h2>{p.name}</h2><p>{p.description||'No description'}</p><div className="progress"><i style={{width:(p.totalTasks?Math.round(p.completedTasks/p.totalTasks*100):0)+'%'}}/></div><div className="card-foot"><span>{p.completedTasks}/{p.totalTasks} tasks</span><span>{p.members.length} members</span></div></Link>)}</div>
 {items.length===0&&<div className="empty">No projects yet. Create your first project.</div>}
 </Layout>
}

function ProjectDetails(){
 const {id}=useParams(), nav=useNavigate()
 const [project,setProject]=useState(null),[tasks,setTasks]=useState([]),[members,setMembers]=useState([]),[newTask,setNewTask]=useState(false),[task,setTask]=useState({title:'',description:'',dueDate:'',status:'TODO',priority:'MEDIUM',assigneeId:''}),[q,setQ]=useState('')
 const load=()=>{api.get('/projects/'+id).then(r=>setProject(r.data));api.get('/tasks/project/'+id).then(r=>setTasks(r.data))}
 useEffect(load,[])
 useEffect(()=>{if(q.length>1)api.get('/projects/users/search?q='+encodeURIComponent(q)).then(r=>setMembers(r.data))},[q])
 async function createTask(e){e.preventDefault();await api.post('/tasks/project/'+id,{...task,assigneeId:task.assigneeId?Number(task.assigneeId):null});setTask({title:'',description:'',dueDate:'',status:'TODO',priority:'MEDIUM',assigneeId:''});setNewTask(false);load()}
 async function status(t,status){await api.put('/tasks/'+t.id,{...t,status,assigneeId:t.assigneeId});load()}
 async function remove(){if(confirm('Delete this project?')){await api.delete('/projects/'+id);nav('/projects')}}
 if(!project)return <Layout><p>Loading...</p></Layout>
 return <Layout><header><div><Link to="/projects" className="back">← Projects</Link><h1>{project.name}</h1><p>{project.description}</p></div><button className="danger" onClick={remove}>Delete</button></header>
 <div className="detail-grid"><section className="panel"><div className="panel-head"><h2>Tasks</h2><button className="primary small" onClick={()=>setNewTask(true)}>+ Add task</button></div>
 <div className="board">{['TODO','IN_PROGRESS','COMPLETED'].map(s=><div className="column" key={s}><h3>{s.replace('_',' ')}</h3>{tasks.filter(t=>t.status===s).map(t=><TaskCard key={t.id} task={t} onStatus={status}/>)}</div>)}</div></section>
 <section className="panel"><h2>Team</h2><div className="members">{project.members.map(m=><div className="member" key={m.id}><div className="avatar">{m.name[0]}</div><div><b>{m.name}</b><small>{m.email}</small></div></div>)}</div><input placeholder="Search user by name/email" value={q} onChange={e=>setQ(e.target.value)}/>{q&&members.map(m=><button className="user-result" key={m.id} onClick={async()=>{await api.post('/projects/'+id+'/members/'+m.id);setQ('');load()}}>{m.name} · {m.email}</button>)}</section></div>
 {newTask&&<Modal title="Create task" close={()=>setNewTask(false)}><form onSubmit={createTask} className="form"><input placeholder="Task title" required value={task.title} onChange={e=>setTask({...task,title:e.target.value})}/><textarea placeholder="Description" value={task.description} onChange={e=>setTask({...task,description:e.target.value})}/><div className="form2"><input type="date" value={task.dueDate} onChange={e=>setTask({...task,dueDate:e.target.value})}/><select value={task.priority} onChange={e=>setTask({...task,priority:e.target.value})}><option>LOW</option><option>MEDIUM</option><option>HIGH</option></select></div><select value={task.assigneeId} onChange={e=>setTask({...task,assigneeId:e.target.value})}><option value="">Unassigned</option>{project.members.map(m=><option value={m.id} key={m.id}>{m.name}</option>)}</select><button className="primary">Create task</button></form></Modal>}
 </Layout>
}
function TaskCard({task,onStatus}){return <div className="task"><div className="task-meta"><span className={'priority '+task.priority.toLowerCase()}>{task.priority}</span>{task.dueDate&&<small>{task.dueDate}</small>}</div><h4>{task.title}</h4><p>{task.description}</p>{task.assigneeName&&<small>Assigned to {task.assigneeName}</small>}<select value={task.status} onChange={e=>onStatus(task,e.target.value)}><option value="TODO">Todo</option><option value="IN_PROGRESS">In progress</option><option value="COMPLETED">Completed</option></select></div>}
function Modal({title,close,children}){return <div className="overlay"><div className="modal"><div className="modal-head"><h2>{title}</h2><button onClick={close}>×</button></div>{children}</div></div>}

export default function App(){
 return <Routes>
   <Route path="/login" element={<Auth/>}/>
   <Route path="/register" element={<Auth register/>}/>
   <Route path="/" element={<Protected><Dashboard/></Protected>}/>
   <Route path="/projects" element={<Protected><Projects/></Protected>}/>
   <Route path="/projects/:id" element={<Protected><ProjectDetails/></Protected>}/>
   <Route path="*" element={<Navigate to="/"/>}/>
 </Routes>
}
