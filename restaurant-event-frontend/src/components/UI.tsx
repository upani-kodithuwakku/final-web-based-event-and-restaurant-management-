import { useEffect, useRef, type ReactNode } from 'react';
import { XMarkIcon, ArrowRightIcon } from '@heroicons/react/24/outline';
export function Modal({title, children, onClose}: {title: string; children: ReactNode; onClose: () => void}) {
 const ref = useRef<HTMLDivElement>(null);
 const onCloseRef = useRef(onClose);
 onCloseRef.current = onClose;
 useEffect(() => { const previous = document.activeElement as HTMLElement; const old = document.body.style.overflow; document.body.style.overflow = 'hidden'; ref.current?.focus(); const key = (e: KeyboardEvent) => {if(e.key === 'Escape') onCloseRef.current(); if(e.key === 'Tab') {const elements = ref.current?.querySelectorAll<HTMLElement>('button, input, select, textarea, a[href], [tabindex="0"]'); if(!elements?.length) return; const first = elements[0], last = elements[elements.length - 1]; if(e.shiftKey && (document.activeElement === first || document.activeElement === ref.current)) {e.preventDefault(); last.focus();} else if(!e.shiftKey && document.activeElement === last) {e.preventDefault(); first.focus();}}}; document.addEventListener('keydown', key); return () => {document.body.style.overflow = old; document.removeEventListener('keydown', key); previous?.focus();}; }, []);
 return <div className="modal-overlay" onClick={e => {if(e.target === e.currentTarget) onClose();}}><div className="modal" ref={ref} role="dialog" aria-modal="true" aria-label={title} tabIndex={-1}><header><h2>{title}</h2><button className="icon-button" onClick={onClose} aria-label="Close dialog"><XMarkIcon/></button></header><div className="modal-body">{children}</div></div></div>;
}
export const Badge = ({status}: {status: string}) => <span className={`badge ${status.toLowerCase().replaceAll('_', '-')}`}><span/> {status.replaceAll('_', ' ').toLowerCase()}</span>;
export const Empty = ({title, children}: {title: string; children?: ReactNode}) => <div className="empty"><div className="empty-icon">✧</div><h3>{title}</h3>{children}</div>;
export const SectionHeading = ({eyebrow, title, description, action}: {eyebrow?: string; title: string; description?: string; action?: ReactNode}) => <div className="section-heading"><div>{eyebrow && <p className="eyebrow">{eyebrow}</p>}<h2>{title}</h2>{description && <p>{description}</p>}</div>{action}</div>;
export const Arrow = () => <ArrowRightIcon className="inline-icon"/>;
