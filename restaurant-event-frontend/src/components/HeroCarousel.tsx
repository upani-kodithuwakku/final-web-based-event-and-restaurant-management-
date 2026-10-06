import { useState, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRightIcon, ChevronLeftIcon, ChevronRightIcon, MapPinIcon } from '@heroicons/react/24/outline';
import { images, photo } from '../data';

type Slide = { image: string; label: string; kicker: string; title: ReactNode; text: ReactNode; cta: string; to: string };

const SLIDES: Slide[] = [
  {
    image: images.hero, label: 'Dining room',
    kicker: 'A LITTLE ESCAPE, RIGHT HERE IN COLOMBO',
    title: <>Good food.<br />Great company.<br /><em>Your kind of place.</em></>,
    text: <>Find your favorite corner. Share something delicious.<br />Make an ordinary day a little more memorable.</>,
    cta: 'Find your table', to: '#spaces',
  },
  {
    image: photo('photo-1414235077428-338989a2e8c0', 1800), label: 'Our menu',
    kicker: 'FRESH FROM OUR KITCHEN',
    title: <>Made with love.<br /><em>Shared with you.</em></>,
    text: <>Wood-fired favorites, fresh salads, and sweet endings.<br />Browse the menu and order straight to your table.</>,
    cta: 'See the menu', to: '/menu',
  },
  {
    image: photo('photo-1511795409834-ef04bbd61622', 1800), label: 'Celebrations',
    kicker: 'BIRTHDAYS, WEDDINGS & EVERYTHING BETWEEN',
    title: <>Moments worth<br /><em>celebrating.</em></>,
    text: <>Private halls, curated packages, and a team that<br />takes care of every little detail.</>,
    cta: 'Plan a celebration', to: '/events',
  },
  {
    image: photo('photo-1519214605650-76a613ee3245', 1800), label: 'The bar',
    kicker: 'AFTER DARK AT GATHER',
    title: <>Good drinks.<br /><em>Late nights.</em></>,
    text: <>Signature cocktails and fresh coolers,<br />made for evenings that run a little late.</>,
    cta: 'See the drinks', to: '/menu',
  },
  {
    image: photo('photo-1552566626-52f8b828add9', 1800), label: 'By the window',
    kicker: 'A SEAT WITH A VIEW',
    title: <>The best seat<br /><em>in the house.</em></>,
    text: <>Watch the city go by from a cosy window table,<br />perfect for two or a small group of friends.</>,
    cta: 'Reserve by the window', to: '#spaces',
  },
];

// Slide timing lives in index.css (heroProgress, 6s).
const shade = (url: string) => `linear-gradient(90deg,rgba(17,29,24,.72),rgba(17,29,24,.08)),url(${url})`;
const cardShade = (url: string) => `linear-gradient(180deg,rgba(0,0,0,0) 35%,rgba(0,0,0,.75)),url(${url})`;

export default function HeroCarousel() {
  const [index, setIndex] = useState(0);
  const [paused, setPaused] = useState(false);
  const go = (i: number) => setIndex((i + SLIDES.length) % SLIDES.length);
  const slide = SLIDES[index];
  const upcoming = [1, 2, 3].map(step => (index + step) % SLIDES.length);

  return (
    <section className={`hero${paused ? ' paused' : ''}`} aria-roledescription="carousel" aria-label="Featured at Gather"
      onMouseEnter={() => setPaused(true)} onMouseLeave={() => setPaused(false)}
      onFocus={() => setPaused(true)} onBlur={() => setPaused(false)}>
      {SLIDES.map((s, i) => (
        <div key={s.label} className={`hero-slide${i === index ? ' active' : ''}`} style={{ backgroundImage: shade(s.image) }} aria-hidden={i !== index} />
      ))}

      {/* Keyed so the text slides up again on every change. */}
      <div className="hero-content" key={index} aria-roledescription="slide" aria-label={`${index + 1} of ${SLIDES.length}`}>
        <span className="hero-kicker"><span /> {slide.kicker}</span>
        <h1>{slide.title}</h1>
        <p>{slide.text}</p>
        {slide.to.startsWith('#')
          ? <a href={slide.to} className="hero-link">{slide.cta} <ArrowRightIcon /></a>
          : <Link to={slide.to} className="hero-link">{slide.cta} <ArrowRightIcon /></Link>}
      </div>

      <div className="hero-rail">
        <div className="hero-cards">
          {upcoming.map(i => (
            <button key={`${index}-${i}`} type="button" className="hero-card" style={{ backgroundImage: cardShade(SLIDES[i].image) }}
              onClick={() => go(i)} aria-label={`Show slide ${i + 1}: ${SLIDES[i].label}`}>
              <small>0{i + 1}</small>
              <b>{SLIDES[i].label}</b>
            </button>
          ))}
        </div>
        <div className="hero-controls">
          <button type="button" className="hero-arrow" onClick={() => go(index - 1)} aria-label="Previous slide"><ChevronLeftIcon /></button>
          <button type="button" className="hero-arrow" onClick={() => go(index + 1)} aria-label="Next slide"><ChevronRightIcon /></button>
          <div className="hero-progress">
            {/* The bar's animation drives auto-advance, so pausing the bar pauses the carousel. */}
            <div key={index} className="hero-progress-fill" onAnimationEnd={() => go(index + 1)} />
          </div>
          <span className="hero-count">0{index + 1}<span> / 0{SLIDES.length}</span></span>
        </div>
      </div>

      <div className="hero-location"><MapPinIcon /> Park Street, Colombo <span>·</span> Est. 2026</div>
    </section>
  );
}
