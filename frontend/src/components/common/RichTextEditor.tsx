import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import Link from '@tiptap/extension-link';
import Placeholder from '@tiptap/extension-placeholder';
import type { CSSProperties } from 'react';
import { useCallback, useRef } from 'react';

// ---------------------------------------------------------------------------
// RichTextEditor — TipTap 2.x wrapper
// ---------------------------------------------------------------------------

export interface RichTextEditorProps {
  /** HTML content (controlled). */
  value?: string;
  /** Called with the updated HTML string on every change. */
  onChange?: (html: string) => void;
  /** Placeholder text shown when the editor is empty. */
  placeholder?: string;
  /** Accessible label for the editor region. */
  ariaLabel?: string;
  /** Called when files are attached via the file input. */
  onFileAttach?: (files: FileList) => void;
}

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------

const wrapper: CSSProperties = {
  border: '1px solid var(--tblr-border-color, #d9dbde)',
  borderRadius: '6px',
  overflow: 'hidden',
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
};

const toolbar: CSSProperties = {
  display: 'flex',
  flexWrap: 'wrap',
  gap: '2px',
  padding: '0.35rem 0.5rem',
  borderBottom: '1px solid var(--tblr-border-color, #d9dbde)',
  backgroundColor: 'var(--tblr-bg-surface-secondary, #fafbfc)',
};

const tbBtn: CSSProperties = {
  minWidth: '32px',
  minHeight: '32px',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '0.2rem 0.4rem',
  border: 'none',
  borderRadius: '4px',
  background: 'transparent',
  cursor: 'pointer',
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: 'var(--tblr-body-color, #1e293b)',
};

const tbBtnActive: CSSProperties = {
  ...tbBtn,
  backgroundColor: 'var(--tblr-primary, rgb(254,201,92))',
  color: 'var(--tblr-primary-fg, #1e293b)',
};

const editorArea: CSSProperties = {
  padding: '0.75rem',
  minHeight: '10rem',
  fontSize: '0.875rem',
  lineHeight: 1.6,
  outline: 'none',
};

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

export default function RichTextEditor({
  value = '',
  onChange,
  placeholder = 'Write something…',
  ariaLabel = 'Rich text editor',
  onFileAttach,
}: RichTextEditorProps) {
  const fileRef = useRef<HTMLInputElement>(null);

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        heading: false, // keep it simple for followups/tasks
      }),
      Underline,
      Link.configure({ openOnClick: false, HTMLAttributes: { rel: 'noopener noreferrer' } }),
      Placeholder.configure({ placeholder }),
    ],
    content: value,
    onUpdate: ({ editor: e }) => {
      onChange?.(e.getHTML());
    },
    editorProps: {
      attributes: {
        role: 'textbox',
        'aria-label': ariaLabel,
        'aria-multiline': 'true',
        style: Object.entries(editorArea)
          .map(([k, v]) => `${k.replace(/[A-Z]/g, (m) => `-${m.toLowerCase()}`)}:${v}`)
          .join(';'),
      },
    },
  });

  // Toolbar helpers
  const btn = useCallback(
    (
      label: string,
      icon: string,
      isActive: boolean,
      action: () => void,
    ) => (
      <button
        key={label}
        type="button"
        title={label}
        aria-label={label}
        aria-pressed={isActive}
        style={isActive ? tbBtnActive : tbBtn}
        onMouseDown={(e) => {
          e.preventDefault(); // keep editor focus
          action();
        }}
      >
        {icon}
      </button>
    ),
    [],
  );

  const handleLinkClick = useCallback(() => {
    if (!editor) return;
    const prev = editor.getAttributes('link').href as string | undefined;
    const url = window.prompt('URL', prev ?? 'https://');
    if (url === null) return;
    if (url === '') {
      editor.chain().focus().extendMarkRange('link').unsetLink().run();
    } else {
      editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
    }
  }, [editor]);

  const handleFileChange = useCallback(() => {
    const files = fileRef.current?.files;
    if (files && files.length > 0) {
      onFileAttach?.(files);
      if (fileRef.current) fileRef.current.value = '';
    }
  }, [onFileAttach]);

  if (!editor) return null;

  return (
    <div style={wrapper}>
      {/* Placeholder style injection */}
      <style>{`.ProseMirror p.is-editor-empty:first-child::before{content:attr(data-placeholder);color:var(--tblr-secondary,#606f91);pointer-events:none;float:left;height:0}`}</style>

      <div role="toolbar" aria-label="Formatting options" style={toolbar}>
        {btn('Bold', 'B', editor.isActive('bold'), () => editor.chain().focus().toggleBold().run())}
        {btn('Italic', 'I', editor.isActive('italic'), () => editor.chain().focus().toggleItalic().run())}
        {btn('Underline', 'U', editor.isActive('underline'), () => editor.chain().focus().toggleUnderline().run())}
        <span style={{ width: '1px', alignSelf: 'stretch', backgroundColor: 'var(--tblr-border-color, #d9dbde)', margin: '0 0.25rem' }} aria-hidden="true" />
        {btn('Bullet list', '•', editor.isActive('bulletList'), () => editor.chain().focus().toggleBulletList().run())}
        {btn('Ordered list', '1.', editor.isActive('orderedList'), () => editor.chain().focus().toggleOrderedList().run())}
        <span style={{ width: '1px', alignSelf: 'stretch', backgroundColor: 'var(--tblr-border-color, #d9dbde)', margin: '0 0.25rem' }} aria-hidden="true" />
        {btn('Code block', '<>', editor.isActive('codeBlock'), () => editor.chain().focus().toggleCodeBlock().run())}
        {btn('Link', '🔗', editor.isActive('link'), handleLinkClick)}
        {onFileAttach && (
          <>
            <button
              type="button"
              title="Attach file"
              aria-label="Attach file"
              style={tbBtn}
              onClick={() => fileRef.current?.click()}
            >
              📎
            </button>
            <input
              ref={fileRef}
              type="file"
              multiple
              style={{ display: 'none' }}
              onChange={handleFileChange}
              aria-hidden="true"
              tabIndex={-1}
            />
          </>
        )}
      </div>

      <EditorContent editor={editor} />
    </div>
  );
}
