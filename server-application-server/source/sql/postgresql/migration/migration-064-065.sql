-- Migration from 064 to 065

-- Delete orphaned attachment_contents (see LMS-1936)  
DELETE FROM attachment_contents WHERE NOT EXISTS 
    (SELECT id FROM attachments WHERE attachment_contents.id = attachments.exac_id)