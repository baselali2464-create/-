---
name: Luminous Technical Intelligence
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#444653'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#757684'
  outline-variant: '#c4c5d5'
  surface-tint: '#3755c3'
  primary: '#00288e'
  on-primary: '#ffffff'
  primary-container: '#1e40af'
  on-primary-container: '#a8b8ff'
  inverse-primary: '#b8c4ff'
  secondary: '#006398'
  on-secondary: '#ffffff'
  secondary-container: '#5bb8fe'
  on-secondary-container: '#00476e'
  tertiary: '#170cae'
  on-tertiary: '#ffffff'
  tertiary-container: '#3433c3'
  on-tertiary-container: '#b3b5ff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dde1ff'
  primary-fixed-dim: '#b8c4ff'
  on-primary-fixed: '#001453'
  on-primary-fixed-variant: '#173bab'
  secondary-fixed: '#cce5ff'
  secondary-fixed-dim: '#93ccff'
  on-secondary-fixed: '#001d31'
  on-secondary-fixed-variant: '#004b73'
  tertiary-fixed: '#e1e0ff'
  tertiary-fixed-dim: '#c0c1ff'
  on-tertiary-fixed: '#07006c'
  on-tertiary-fixed-variant: '#2f2ebe'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  display-lg:
    fontFamily: IBM Plex Sans
    fontSize: 3rem
    fontWeight: '700'
    lineHeight: 3.75rem
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: IBM Plex Sans
    fontSize: 2.25rem
    fontWeight: '700'
    lineHeight: 2.875rem
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: IBM Plex Sans
    fontSize: 2rem
    fontWeight: '600'
    lineHeight: 2.625rem
  headline-lg-mobile:
    fontFamily: IBM Plex Sans
    fontSize: 1.625rem
    fontWeight: '600'
    lineHeight: 2.25rem
  headline-md:
    fontFamily: IBM Plex Sans
    fontSize: 1.5rem
    fontWeight: '600'
    lineHeight: 2.125rem
  headline-sm:
    fontFamily: IBM Plex Sans
    fontSize: 1.25rem
    fontWeight: '600'
    lineHeight: 1.75rem
  title-md:
    fontFamily: IBM Plex Sans
    fontSize: 1.125rem
    fontWeight: '500'
    lineHeight: 1.625rem
  body-lg:
    fontFamily: IBM Plex Sans
    fontSize: 1.125rem
    fontWeight: '400'
    lineHeight: 1.875rem
  body-md:
    fontFamily: IBM Plex Sans
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: 1.75rem
  body-sm:
    fontFamily: IBM Plex Sans
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: 1.5rem
  label-lg:
    fontFamily: IBM Plex Sans
    fontSize: 0.875rem
    fontWeight: '600'
    lineHeight: 1.25rem
  label-md:
    fontFamily: IBM Plex Sans
    fontSize: 0.75rem
    fontWeight: '500'
    lineHeight: 1rem
  label-sm:
    fontFamily: IBM Plex Sans
    fontSize: 0.6875rem
    fontWeight: '500'
    lineHeight: 0.875rem
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-tablet: 1.5rem
  gutter-desktop: 2rem
  margin: 1rem
  margin-tablet: 2rem
  margin-desktop: 3rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

This design system expresses academic precision, cognitive clarity, and adaptive digital intelligence. Designed for ambitious learners, engineers, and educational institutions, it merges the authority of formal technical education with the agility of real-time AI assistance.

The visual direction follows **Modern Precision Minimal with Tonal Depth**:
- **Purity & Focus**: High-contrast, pristine light backdrops eliminate visual noise and direct cognitive resources strictly to learning content, code editors, and assessment metrics.
- **Academic Reliability Meets AI Dynamism**: Deep cobalt structure establishes rigor and institutional trust, while electric cyan accents highlight progressive states, predictive insights, and active pathways.
- **RTL-Native Ergonomics**: Engineered from the ground up for right-to-left layout dynamics, prioritizing thumb-reach micro-interactions, natural reading sweeps, and clear visual hierarchy for bilingual (Arabic/Latin) code and data displays.

## Colors

The color palette centers on a disciplined range of intellectual blues and slates, engineered to balance extended reading sessions with punchy interactive feedback.

- **Primary (`#1E40AF`)**: Deep Royal Blue. Anchors key navigation, master action buttons, structural headings, and verified academic credentials. It communicates stability and institutional authority.
- **Secondary (`#0284C7`)**: Bright Electric Sky. Used for active states, interactive tags, AI-assisted suggestions, and live quiz progress markers.
- **Tertiary (`#6366F1`)**: Indigo / Violet accent. Highlights advanced tech tracks, dynamic achievements, analytics sparklines, and algorithmic course recommendations.
- **Neutral (`#0F172A`)**: Deep Slate. Supplies crisp, high-contrast typography and icon framing without the harshness of pure black. Paired with secondary text in `#475569` and subtle borders in `#E2E8F0`.
- **Canvas & Surfaces**: The base canvas utilizes `#F8FAFC` (Slate 50), with elevated functional cards resting on `#FFFFFF` to ensure deep contrast and tactile distinction.

## Typography

The type system is driven by **IBM Plex Sans** (with its native Arabic partner IBM Plex Sans Arabic). It offers a systematic, technical tone that pairs industrial clarity with Arabic calligraphic proportion.

- **Vertical Proportion**: Arabic glyphs possess extended ascenders and descenders; line heights are expanded to a generous `1.75`–`1.875` ratio on body text to prevent visual collisions and eyestrain.
- **Numbers & Data**: All numerical expressions, scores, and timestamps default to tabular figures to ensure alignment in course grading grids, code syntax blocks, and lesson completion trackers.
- **Bilingual Symbiosis**: Latin terms and Arabic terminology balance effortlessly at identical cap-heights without requiring artificial scale adjustments.

## Layout & Spacing

The layout is built around an RTL-first, responsive column structure with an 8pt baseline rhythmic flow:

- **Mobile (<768px)**: 4 fluid columns, `1rem` margin, `1rem` gutter. Primary CTAs, lesson nav triggers, and floating controls pin to the bottom thumb zone.
- **Tablet (768px–1023px)**: 8 fluid columns, `2rem` margin, `1.5rem` gutter. Split-screen layout separates lecture/code panels from notes and interactive quizzes.
- **Desktop (1024px+)**: 12 fluid columns, constrained to a maximum content container of `1280px` centered on wide displays, with `3rem` outer canvas padding and `2rem` internal gutters.

Spacing tokens dictate distances exclusively:
- `space-xs` and `space-sm` serve inline badge offsets, icon-to-label gaps, and compact checklist items.
- `space-md` acts as standard form stack gaps, list intervals, and mobile card padding.
- `space-lg` governs card inner padding, section headers, and metric card clusters.
- `space-xl` enforces major editorial breaks between curriculum modules and syllabus blocks.

## Elevation & Depth

Visual hierarchy is built using ultra-soft ambient shadow tinting and crisp hairline borders:

- **Flat / Ground Level (Canvas)**: Background tint `#F8FAFC` without elevation. Used for base page bodies and secondary dashboard segments.
- **Tier 1 (Surface Cards & Inputs)**: Background `#FFFFFF`, bordered with a 1px solid hairline (`#E2E8F0`), elevated by a soft ambient shadow: `0 1px 3px 0 rgba(15, 23, 42, 0.04), 0 1px 2px -1px rgba(15, 23, 42, 0.04)`.
- **Tier 2 (Active Cards, Hover States & Sticky Headers)**: Background `#FFFFFF`, subtle outline (`#CBD5E1`), shadow: `0 10px 15px -3px rgba(30, 64, 175, 0.06), 0 4px 6px -4px rgba(30, 64, 175, 0.04)`. Note the subtle blue tint in the dispersion.
- **Tier 3 (Modals, Overlays & Floating Drawers)**: Pure white surface with deep protective ambient occlusion: `0 20px 25px -5px rgba(15, 23, 42, 0.1), 0 8px 10px -6px rgba(15, 23, 42, 0.08)`.

## Shapes

The geometric identity combines modern approachability with crisp architectural restraint:

- **Standard Elements (`rounded-md`, 0.5rem)**: Applied to input elements, small buttons, dropdown triggers, and status badges.
- **Card Containers (`rounded-lg`, 1rem)**: Used for content modules, course cards, interactive syntax blocks, and analytics tiles.
- **Prominent Structural Elements (`rounded-xl`, 1.5rem)**: Reserved for hero learning banners, mobile bottom sheets, modal dialogs, and highlighted certification awards.
- **Full Pill (`rounded-full`)**: Strictly reserved for micro-tags, notification badges, avatar containers, and floating audio/media pill controllers.

## Components

### Buttons
- **Primary Action**: Solid `#1E40AF` fill with `#FFFFFF` text. Minimum touch target height of 48px on mobile (`space-md` horizontal padding, `0.75rem` vertical). Active states shift to `#1D4ED8` with a 2px offset focus ring.
- **Secondary Action**: Tinted surface (`#EFF6FF`), `#1E40AF` label, no border.
- **Ghost/Tertiary**: Transparent surface with `#0F172A` text, shifting to `#F1F5F9` on hover.

### Chips & Badges
- **Status / Level Chips**: Height of 28px, `rounded-full` radius, `0.75rem` horizontal padding. Font: `label-md`.
- **Color Rules**: Technical tracks use Tertiary tint (`#EEF2FF` background, `#4F46E5` text). AI-assisted items use Secondary tint (`#E0F2FE` background, `#0284C7` text).

### Form Inputs & Search
- **Structure**: 48px height, `0.5rem` radius, white background, `1px solid #CBD5E1`.
- **RTL Iconography**: Prefix icons anchor to the far right, placeholder text begins at `right: 2.75rem`.
- **Focus State**: 1px border transitions to `#2563EB` with an ambient outer ring: `box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15)`.

### Cards & Lesson Modules
- Built on white backgrounds with 1px border (`#E2E8F0`) and `rounded-lg` (1rem) corners.
- Padding adapts from `space-md` (1rem) on mobile to `space-lg` (1.5rem) on desktop.
- Header contains title and track badge; body encapsulates progress status or meta metrics; footer accommodates thumb-friendly direct action triggers.

### Progress Indicators
- **Linear Progress Bar**: 8px track height, `rounded-full`, base track `#E2E8F0`, animated active fill via linear gradient (`#1E40AF` to `#0284C7`). Direction flows naturally right-to-left.
- **Circular Milestone Trackers**: 4px stroke width with smooth SVG offset strokes and centered bold numerical values.

### Checkboxes & Radio Controls
- Minimum dimensions of 20x20px with a 44x44px invisible tap container.
- Selected state fills with `#1E40AF`, presenting a crisp white checkmark icon. Smooth spring transition on activation.